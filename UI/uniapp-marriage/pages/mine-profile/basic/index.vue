<template>
  <view class="page">
    <su-navbar title="我的资料" statusBar />

    <scroll-view class="scroll" scroll-y enhanced>
      <view class="content">
        <view class="intro card">
          <text class="title">完善个人资料，提升展示效果</text>
          <text class="description">真实、完整的资料有助于提升个人可信度，帮助你获得更精准的推荐与更多关注。</text>
        </view>
        <view class="auth card" @tap="goCertification">
          <image src="/static/certification/shield.svg" mode="aspectFit" />
          <view class="auth-copy">
            <text class="auth-title">{{ verified ? '已完成实名认证' : '完成实名认证' }}</text>
            <text class="description">{{ verified ? '当前实名状态已同步，可继续完善资料与相册。' : '认证后资料可信度更高，也有助于平台进行安全审核。' }}</text>
          </view>
          <text class="auth-link">{{ verified ? '已认证' : '去认证' }}</text>
        </view>
        <view class="tabs card">
          <view v-for="tab in tabs" :key="tab.key" :class="{ active: state.tab === tab.key }" @tap="state.tab = tab.key">{{ tab.label }}</view>
        </view>

        <view v-if="state.loading" class="section card status-card">
          <text class="description">资料加载中...</text>
        </view>
        <view v-else-if="state.loadError" class="section card status-card">
          <text class="description">{{ state.loadError }}</text>
          <text class="retry" @tap="load">重新加载</text>
        </view>

        <template v-else-if="state.tab === 'profile'">
          <view class="section card">
            <view class="section-head"><text class="title">基础信息</text><text class="meta">{{ completion }}% 已完善</text></view>
            <view class="row" @tap="editNickname"><text>昵称</text><view class="value">{{ form.nickname || '未填写' }}<view class="arrow" /></view></view>
            <picker :disabled="verified" :range="['男','女']" :value="Math.max(0, Number(form.sex)-1)" @change="form.sex=Number($event.detail.value)+1">
              <view class="row" :class="{ locked: verified }"><text>性别</text><view class="value">{{ sexText || '未填写' }}<text v-if="verified" class="lock">已锁定</text><view v-else class="arrow" /></view></view>
            </picker>
            <picker :disabled="verified" mode="date" :value="form.birthday" start="1950-01-01" :end="today" @change="form.birthday=$event.detail.value">
              <view class="row" :class="{ locked: verified }"><text>生日</text><view class="value">{{ form.birthday || '未填写' }}<text v-if="verified" class="lock">已锁定</text><view v-else class="arrow" /></view></view>
            </picker>
            <picker mode="multiSelector" :range="homeAreaColumns" range-key="name" :value="state.areaIndexes.home" @columnchange="onAreaColumnChange('home', $event)" @change="onAreaChange('home', $event)">
              <view class="row"><text>家乡</text><view class="value">{{ areaText(form.areaId) || '未填写' }}<view class="arrow" /></view></view>
            </picker>
          </view>

          <view class="section card">
            <view class="section-head"><text class="title">婚恋资料</text><text class="meta">继续完善，提升个人吸引力</text></view>
            <picker v-for="item in marriageFieldsBeforeArea" :key="item.field" :range="fieldOptions(item)" :value="fieldIndex(form[item.field], item)" @change="onFieldChange(form, item, $event)">
              <view class="row"><text>{{ item.label }}</text><view class="value">{{ fieldText(form[item.field], item) || '未填写' }}<view class="arrow" /></view></view>
            </picker>
            <picker mode="multiSelector" :range="liveAreaColumns" range-key="name" :value="state.areaIndexes.live" @columnchange="onAreaColumnChange('live', $event)" @change="onAreaChange('live', $event)">
              <view class="row"><text>现居地</text><view class="value">{{ areaText(form.liveAreaId) || '未填写' }}<view class="arrow" /></view></view>
            </picker>
            <picker v-for="item in marriageFieldsAfterArea" :key="item.field" :range="fieldOptions(item)" :value="fieldIndex(form[item.field], item)" @change="onFieldChange(form, item, $event)">
              <view class="row"><text>{{ item.label }}</text><view class="value">{{ fieldText(form[item.field], item) || '未填写' }}<view class="arrow" /></view></view>
            </picker>
          </view>

          <view class="section card">
            <view class="section-head"><text class="title">自我介绍</text><text class="meta">{{ (form.bio || '').length }}/500</text></view>
            <textarea v-model="form.bio" class="bio" maxlength="500" placeholder="介绍一下你的性格、生活状态和期待，让对方更快了解你" placeholder-class="bio-placeholder" />
          </view>

          <view class="section card">
            <view class="section-head"><text class="title">我的相册</text><text class="meta">最多上传 9 张</text></view>
            <view class="avatar">
              <image :src="avatarUrl" mode="aspectFill" />
              <view><text class="avatar-name">{{ form.nickname || '微信用户' }}</text><text class="description">头像在“我的”首页更换，相册用于推荐卡片和个人主页展示。</text></view>
            </view>
            <view class="album">
              <view v-for="img in state.album" :key="img.id" class="photo">
                <image :src="cdn(img.url)" mode="aspectFill" /><view class="delete" @tap.stop="removePhoto(img)">×</view>
              </view>
              <view v-if="state.album.length < 9" class="add" @tap="addPhoto"><text class="plus">+</text><text>添加照片</text></view>
            </view>
          </view>
        </template>

        <view v-else-if="state.tab === 'preference'" class="section card">
          <view class="section-head"><text class="title">理想对象</text><text class="meta">根据真实期待填写</text></view>
          <picker :range="dictLabels('partner_marital_status')" :value="dictIndex(preference.mateMaritalStatus, 'partner_marital_status')" @change="onDictChange(preference, 'mateMaritalStatus', 'partner_marital_status', $event)">
            <view class="row"><text>婚姻状态</text><view class="value">{{ dictLabel(preference.mateMaritalStatus, 'partner_marital_status') || '未填写' }}<view class="arrow" /></view></view>
          </picker>
          <picker mode="multiSelector" :range="[heightOptions, heightOptions]" :value="heightRangeIndexes" @change="onRangeChange('height', $event)">
            <view class="row"><text>身高范围</text><view class="value">{{ heightRangeText || '未填写' }}<view class="arrow" /></view></view>
          </picker>
          <picker mode="multiSelector" :range="[weightOptions, weightOptions]" :value="weightRangeIndexes" @change="onRangeChange('weight', $event)">
            <view class="row"><text>体重范围</text><view class="value">{{ weightRangeText || '未填写' }}<view class="arrow" /></view></view>
          </picker>
          <picker :range="dictLabels('partner_education')" :value="dictIndex(preference.mateMinEducation, 'partner_education')" @change="onDictChange(preference, 'mateMinEducation', 'partner_education', $event)">
            <view class="row"><text>学历</text><view class="value">{{ dictLabel(preference.mateMinEducation, 'partner_education') || '未填写' }}<view class="arrow" /></view></view>
          </picker>
          <picker mode="multiSelector" :range="mateAreaColumns" range-key="name" :value="state.areaIndexes.mate" @columnchange="onAreaColumnChange('mate', $event)" @change="onAreaChange('mate', $event)">
            <view class="row"><text>现居地</text><view class="value">{{ areaText(preference.mateLiveAreaId) || '未填写' }}<view class="arrow" /></view></view>
          </picker>
          <picker v-for="item in preferenceFields" :key="item.field" :range="fieldOptions(item)" :value="fieldIndex(preference[item.field], item)" @change="onFieldChange(preference, item, $event)">
            <view class="row"><text>{{ item.label }}</text><view class="value">{{ fieldText(preference[item.field], item) || '未填写' }}<view class="arrow" /></view></view>
          </picker>
        </view>

        <view v-else class="section card preview">
          <image class="preview-cover" :src="previewCover" mode="aspectFill" />
          <view class="preview-title-row"><image :src="avatarUrl" mode="aspectFill" /><view><text class="title">{{ form.nickname || '微信用户' }}</text><text class="description">{{ previewSummary }}</text></view></view>
          <text class="preview-bio">{{ form.bio || '暂未填写自我介绍' }}</text>
          <view v-if="profileTags.length" class="tag-list"><text v-for="tag in profileTags" :key="tag" class="tag">{{ tag }}</text></view>
          <view class="preview-subsection"><text class="preview-subtitle">择偶条件</text><view v-if="preferenceTags.length" class="tag-list"><text v-for="tag in preferenceTags" :key="tag" class="tag preference-tag">{{ tag }}</text></view><text v-else class="description">暂未填写择偶条件</text></view>
          <view v-if="state.album.length" class="preview-album"><image v-for="img in state.album" :key="img.id" :src="cdn(img.url)" mode="aspectFill" /></view>
        </view>

        <view v-if="!state.loading" class="tip card"><text class="title">温馨提示</text><text class="description">{{ state.tab === 'profile' ? '请如实填写并保持资料完整，清晰的介绍和照片会帮助对方更快了解你。' : '条件设置越清晰，推荐越贴近你的期待；适当保留弹性，也有助于发现更多合适的人。' }}</text></view>
      </view>
    </scroll-view>
    <view v-if="!state.loading && !state.loadError" class="save-bar"><view class="save" @tap="save">{{ state.saving ? '保存中...' : '保存修改' }}</view></view>
  </view>
</template>

<script>
  import { computed, defineComponent, reactive } from 'vue';
  import { onShow } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import ProfileApi from '@/sheep/api/marriage/profile';
  import DictApi from '@/sheep/api/system/dict';
  import AreaApi from '@/sheep/api/system/area';
  import FileApi from '@/sheep/api/infra/file';

  export default defineComponent({
    setup() {
      const types = ['partner_education','partner_income_level','partner_profession','partner_marital_status','partner_house_status','partner_car_status'];
      const tabs = [{key:'profile',label:'资料与相册'},{key:'preference',label:'择偶条件'},{key:'preview',label:'预览主页'}];
      const state = reactive({ tab:'profile', loading:false, loadError:'', saving:false, album:[], areas:[], loadGeneration:0, areaIndexes:{home:[0,0,0],live:[0,0,0],mate:[0,0,0]} });
      const dicts = reactive(Object.fromEntries(types.map(x=>[x,[]])));
      const form = reactive({ nickname:'',sex:0,birthday:'',areaId:'',maritalStatus:'',heightCm:'',weightKg:'',education:'',incomeLevel:'',liveAreaId:'',houseStatus:'',carStatus:'',jobTitle:'',bio:'',avatar:'',backgroundImage:'',realVerified:0,profileCompletion:0 });
      const preference = reactive({ mateMaritalStatus:'',mateMinHeightCm:'',mateMaxHeightCm:'',mateMinWeightKg:'',mateMaxWeightKg:'',mateMinEducation:'',mateLiveAreaId:'',mateMinIncomeLevel:'',mateHouseStatus:'',mateCarStatus:'',mateJobTitle:'' });
      const verified = computed(()=>Number(form.realVerified)===1);
      const sexText = computed(()=>Number(form.sex)===1?'男':Number(form.sex)===2?'女':'');
      const completion = computed(()=>Number(form.profileCompletion)||Math.round(Object.values(form).filter(Boolean).length/14*100));
      const today = new Date().toISOString().slice(0,10);
      const cdn = url=>url?sheep.$url.cdn(url):'';
      const avatarUrl = computed(()=>cdn(form.avatar)||sheep.$url.static('/static/img/shop/default_avatar.png'));
      const heightOptions = Array.from({length:81},(_,index)=>`${140+index}cm`);
      const weightOptions = Array.from({length:116},(_,index)=>`${35+index}kg`);
      const marriageFieldsBeforeArea = computed(()=>[
        item('婚姻状态','maritalStatus','partner_marital_status'), item('身高','heightCm','number',140,220,'cm'),
        item('体重','weightKg','number',35,150,'kg'), item('学历','education','partner_education'),
        item('收入档位','incomeLevel','partner_income_level')
      ]);
      const marriageFieldsAfterArea = computed(()=>[
        item('房产情况','houseStatus','partner_house_status'),
        item('车辆情况','carStatus','partner_car_status'), item('职业 / 职位','jobTitle','partner_profession',0,0,'',false,true)
      ]);
      const preferenceFields = computed(()=>[
        item('收入档位','mateMinIncomeLevel','partner_income_level',0,0,'',true),
        item('房产情况','mateHouseStatus','partner_house_status',0,0,'',true), item('车辆情况','mateCarStatus','partner_car_status',0,0,'',true),
        item('职业','mateJobTitle','partner_profession',0,0,'',true,true)
      ]);
      function item(label,field,type,min=0,max=0,unit='',pref=false,text=false){return{label,field,type,min,max,unit,pref,text};}
      function areaText(id){const path=[];const walk=nodes=>(nodes||[]).some(n=>{path.push(n.name);if(String(n.id)===String(id)||walk(n.children))return true;path.pop();return false;});return id&&walk(state.areas)?path.join(' '):'';}
      function dictLabels(type){return (dicts[type]||[]).map(x=>x.label);}
      function dictLabel(value,type){return (dicts[type]||[]).find(x=>String(x.value)===String(value))?.label||'';}
      function dictIndex(value,type){return Math.max(0,(dicts[type]||[]).findIndex(x=>String(x.value)===String(value)));}
      function onDictChange(model,field,type,event){const selected=(dicts[type]||[])[Number(event.detail.value)];if(selected)model[field]=selected.value;}
      function fieldOptions(field){return field.type==='number'?Array.from({length:field.max-field.min+1},(_,index)=>`${field.min+index}${field.unit}`):dictLabels(field.type);}
      function fieldIndex(value,field){if(field.type==='number')return Math.max(0,Number(value||field.min)-field.min);return dictIndex(value,field.type);}
      function fieldText(value,field){if(value===''||value===null||value===undefined)return '';return field.type==='number'?`${value}${field.unit}`:(field.text?String(value):dictLabel(value,field.type));}
      function onFieldChange(model,field,event){const index=Number(event.detail.value);if(field.type==='number'){model[field.field]=field.min+index;return;}const selected=(dicts[field.type]||[])[index];if(selected)model[field.field]=field.text?selected.label:selected.value;}
      function areaColumns(key){const indexes=state.areaIndexes[key];const provinces=state.areas||[];const cities=provinces[indexes[0]]?.children||[];const districts=cities[indexes[1]]?.children||[];return[provinces,cities,districts];}
      const homeAreaColumns=computed(()=>areaColumns('home'));
      const liveAreaColumns=computed(()=>areaColumns('live'));
      const mateAreaColumns=computed(()=>areaColumns('mate'));
      function onAreaColumnChange(key,event){const indexes=state.areaIndexes[key],column=Number(event.detail.column);indexes[column]=Number(event.detail.value);if(column===0){indexes[1]=0;indexes[2]=0;}else if(column===1)indexes[2]=0;}
      function onAreaChange(key,event){const indexes=event.detail.value.map(Number);state.areaIndexes[key]=indexes;const columns=areaColumns(key),selected=columns[2][indexes[2]]||columns[1][indexes[1]]||columns[0][indexes[0]];if(key==='home')form.areaId=selected?.id||'';else if(key==='live')form.liveAreaId=selected?.id||'';else preference.mateLiveAreaId=selected?.id||'';}
      function findAreaIndexes(id){let found=[0,0,0];state.areas.some((province,a)=>(province.children||[]).some((city,b)=>{const c=(city.children||[]).findIndex(district=>String(district.id)===String(id));if(c>=0){found=[a,b,c];return true;}if(String(city.id)===String(id)){found=[a,b,0];return true;}return false;})||String(province.id)===String(id)&&(found=[a,0,0]));return found;}
      function syncAreaIndexes(){state.areaIndexes.home=findAreaIndexes(form.areaId);state.areaIndexes.live=findAreaIndexes(form.liveAreaId);state.areaIndexes.mate=findAreaIndexes(preference.mateLiveAreaId);}
      const heightRangeIndexes=computed(()=>[Math.max(0,Number(preference.mateMinHeightCm||140)-140),Math.max(0,Number(preference.mateMaxHeightCm||140)-140)]);
      const weightRangeIndexes=computed(()=>[Math.max(0,Number(preference.mateMinWeightKg||35)-35),Math.max(0,Number(preference.mateMaxWeightKg||35)-35)]);
      const heightRangeText=computed(()=>preference.mateMinHeightCm||preference.mateMaxHeightCm?`${preference.mateMinHeightCm||140}cm-${preference.mateMaxHeightCm||220}cm`:'' );
      const weightRangeText=computed(()=>preference.mateMinWeightKg||preference.mateMaxWeightKg?`${preference.mateMinWeightKg||35}kg-${preference.mateMaxWeightKg||150}kg`:'' );
      function onRangeChange(type,event){const [minIndex,maxIndex]=event.detail.value.map(Number),minBase=type==='height'?140:35;if(minIndex>maxIndex){uni.showToast({title:'最小值不能大于最大值',icon:'none'});return;}if(type==='height'){preference.mateMinHeightCm=minBase+minIndex;preference.mateMaxHeightCm=minBase+maxIndex;}else{preference.mateMinWeightKg=minBase+minIndex;preference.mateMaxWeightKg=minBase+maxIndex;}}
      async function load(){const generation=++state.loadGeneration;state.loading=true;state.loadError='';try{const rs=await Promise.all([ProfileApi.getMyProfile(),AreaApi.getAreaTree(),FileApi.getAlbumImages('partner_album'),...types.map(type=>DictApi.getDictDataListByType(type))]);if(generation!==state.loadGeneration)return;const failed=rs.find(result=>result?.code!==0);if(failed)throw new Error(failed.msg||'资料加载失败');Object.assign(form,rs[0].data||{});Object.assign(preference,rs[0].data||{});state.areas=rs[1].data||[];state.album=rs[2].data||[];types.forEach((type,index)=>dicts[type]=rs[index+3]?.data||[]);syncAreaIndexes();}catch(error){if(generation===state.loadGeneration)state.loadError=error?.message||'资料加载失败，请稍后重试';}finally{if(generation===state.loadGeneration)state.loading=false;}}
      function editNickname(){uni.showModal({title:'编辑昵称',editable:true,content:form.nickname,success:r=>{if(r.confirm)form.nickname=String(r.content||'').trim().slice(0,30);}})}
      function profilePayload(){return{nickname:String(form.nickname||'').trim(),sex:Number(form.sex)||0,birthday:form.birthday||'',areaId:Number(form.areaId)||0,maritalStatus:Number(form.maritalStatus)||0,heightCm:Number(form.heightCm)||0,weightKg:Number(form.weightKg)||0,education:Number(form.education)||0,incomeLevel:Number(form.incomeLevel)||0,liveAreaId:Number(form.liveAreaId)||0,houseStatus:Number(form.houseStatus)||0,carStatus:Number(form.carStatus)||0,jobTitle:form.jobTitle||'',bio:form.bio||''};}
      function preferencePayload(){return{mateMaritalStatus:Number(preference.mateMaritalStatus)||0,mateMinHeightCm:Number(preference.mateMinHeightCm)||0,mateMaxHeightCm:Number(preference.mateMaxHeightCm)||0,mateMinWeightKg:Number(preference.mateMinWeightKg)||0,mateMaxWeightKg:Number(preference.mateMaxWeightKg)||0,mateMinEducation:Number(preference.mateMinEducation)||0,mateLiveAreaId:Number(preference.mateLiveAreaId)||0,mateMinIncomeLevel:Number(preference.mateMinIncomeLevel)||0,mateHouseStatus:Number(preference.mateHouseStatus)||0,mateCarStatus:Number(preference.mateCarStatus)||0,mateJobTitle:preference.mateJobTitle||'',mateRemark:''};}
      async function save(){if(state.saving)return;if(state.tab==='profile'){form.nickname=String(form.nickname||'').trim();if(!form.nickname){uni.showToast({title:'请输入昵称',icon:'none'});return;}if(form.nickname.length>30){uni.showToast({title:'昵称不能超过 30 个字',icon:'none'});return;}if((form.bio||'').length>500){uni.showToast({title:'自我介绍不能超过 500 个字',icon:'none'});return;}}state.saving=true;try{const response=state.tab==='profile'?await ProfileApi.updateMyProfile(profilePayload()):await ProfileApi.updateMyPreference(preferencePayload());if(response?.code!==0)throw new Error(response?.msg||'保存失败');uni.showToast({title:'保存成功',icon:'success'});setTimeout(()=>sheep.$router.back(),800);}catch(error){uni.showToast({title:error?.message||'保存失败，请稍后重试',icon:'none'});}finally{state.saving=false;}}
      async function addPhoto(){try{const paths=await new Promise(ok=>uni.chooseImage({count:9-state.album.length,sizeType:['compressed'],sourceType:['album','camera'],success:r=>ok(r.tempFilePaths||[]),fail:()=>ok([])}));if(!paths.length)return;for(const path of paths){const uploaded=await FileApi.uploadFile(path,'marriage/album',{bizType:'partner_album'});if(!uploaded||uploaded.code!==0)throw new Error(uploaded?.msg||'照片上传失败');}const response=await FileApi.getAlbumImages('partner_album');if(response?.code!==0)throw new Error(response?.msg||'相册刷新失败');state.album=response.data||[];uni.showToast({title:'上传成功',icon:'success'});}catch(error){uni.showToast({title:error?.message||'照片上传失败，请稍后重试',icon:'none'});}}
      function removePhoto(img){uni.showModal({title:'删除照片',content:'删除后将从个人资料中移除这张照片，是否继续？',success:async result=>{if(!result.confirm)return;try{const response=await FileApi.deleteAlbumImage(img.id);if(response?.code!==0)throw new Error(response?.msg||'删除失败');state.album=state.album.filter(item=>item.id!==img.id);uni.showToast({title:'删除成功',icon:'success'});}catch(error){uni.showToast({title:error?.message||'删除失败，请稍后重试',icon:'none'});}}})}
      const previewCover=computed(()=>cdn(form.backgroundImage)||cdn(state.album[0]?.url)||avatarUrl.value);
      const previewSummary=computed(()=>[sexText.value,form.birthday?`${new Date().getFullYear()-Number(form.birthday.slice(0,4))}岁`:'',areaText(form.liveAreaId)].filter(Boolean).join(' · '));
      const profileTags=computed(()=>[dictLabel(form.maritalStatus,'partner_marital_status'),form.heightCm?`${form.heightCm}cm`:'',form.weightKg?`${form.weightKg}kg`:'',dictLabel(form.education,'partner_education'),dictLabel(form.incomeLevel,'partner_income_level'),dictLabel(form.houseStatus,'partner_house_status'),dictLabel(form.carStatus,'partner_car_status'),form.jobTitle].filter(Boolean));
      const preferenceTags=computed(()=>[dictLabel(preference.mateMaritalStatus,'partner_marital_status'),heightRangeText.value&&`身高 ${heightRangeText.value}`,weightRangeText.value&&`体重 ${weightRangeText.value}`,dictLabel(preference.mateMinEducation,'partner_education'),areaText(preference.mateLiveAreaId)&&`现居 ${areaText(preference.mateLiveAreaId)}`,dictLabel(preference.mateMinIncomeLevel,'partner_income_level'),dictLabel(preference.mateHouseStatus,'partner_house_status'),dictLabel(preference.mateCarStatus,'partner_car_status'),preference.mateJobTitle].filter(Boolean));
      onShow(load);
      return{tabs,state,form,preference,verified,sexText,completion,today,marriageFieldsBeforeArea,marriageFieldsAfterArea,preferenceFields,heightOptions,weightOptions,heightRangeIndexes,weightRangeIndexes,heightRangeText,weightRangeText,homeAreaColumns,liveAreaColumns,mateAreaColumns,previewCover,previewSummary,profileTags,preferenceTags,cdn,avatarUrl,areaText,dictLabels,dictLabel,dictIndex,onDictChange,fieldOptions,fieldIndex,fieldText,onFieldChange,onAreaColumnChange,onAreaChange,onRangeChange,editNickname,load,save,addPhoto,removePhoto,goBack:()=>sheep.$router.back(),goMine:()=>sheep.$router.go('/pages/index/user'),goCertification:()=>sheep.$router.go('/pages/mine-certifications/index')};
    }
  });
</script>

<style>
  .page{position:relative;min-height:100vh;background:#f7f4ef;color:#554342;overflow:hidden}.header{position:absolute;z-index:20;left:0;right:0;top:0;padding:46px 16px 12px}.header-bar{display:flex;height:48px;padding:0 10px;align-items:center;justify-content:space-between;border:1px solid rgba(190,120,120,.12);border-radius:24px;background:rgba(255,253,250,.94);box-shadow:0 10px 24px rgba(176,124,124,.08)}.header-action{display:flex;width:40px;height:40px;align-items:center;justify-content:center}.mine-link{width:72px;justify-content:flex-end;font-size:13px;font-weight:700;color:#b35467}.back-arrow{width:10px;height:10px;margin-left:4px;border-left:2px solid #795858;border-bottom:2px solid #795858;transform:rotate(45deg)}.header-title{font-size:18px;font-weight:700;color:#5d403f}.scroll{position:absolute;inset:0;height:100vh}.content{padding:116px 16px 112px}.card{box-sizing:border-box;border:1px solid rgba(202,173,164,.26);border-radius:24px;background:#fffdfa;box-shadow:0 14px 32px rgba(205,175,160,.14)}.intro,.tip{display:flex;flex-direction:column;padding:18px;margin-bottom:14px}.title{font-size:20px;line-height:26px;font-weight:700;color:#523c3d}.description{font-size:13px;line-height:20px;color:#866c67}.intro .title{margin-bottom:6px}.auth{display:flex;padding:14px 16px;margin-bottom:14px;align-items:center;background:#fff7f7}.auth image{width:42px;height:42px;margin-right:12px;padding:11px;box-sizing:border-box;border-radius:14px;background:#ffe9ef}.auth-copy{display:flex;flex:1;flex-direction:column}.auth-title{font-size:15px;font-weight:800}.auth-link{font-size:13px;font-weight:800;color:#c84449}.tabs{display:flex;height:52px;padding:4px;margin-bottom:14px}.tabs view{display:flex;flex:1;align-items:center;justify-content:center;border-radius:22px;font-size:14px;font-weight:700;color:#8b6f6a}.tabs .active{background:#c45c5c;color:#fff}.section{padding:18px;margin-bottom:14px}.section-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:8px}.meta{max-width:120px;font-size:12px;line-height:18px;text-align:right;color:#aa817a}.row{display:flex;padding:18px 0;align-items:center;justify-content:space-between;border-top:1px solid rgba(202,173,164,.22);font-size:15px}.value{display:flex;flex:1;padding-left:16px;align-items:center;justify-content:flex-end;font-size:14px;color:#785f5a}.arrow{width:8px;height:8px;margin-left:10px;border-top:2px solid #c26d72;border-right:2px solid #c26d72;transform:rotate(45deg)}.locked{opacity:.78}.lock{margin-left:10px;font-size:12px;color:#b35467}.bio{box-sizing:border-box;width:100%;min-height:136px;padding:14px;border-radius:18px;background:#fbf4ef;font-size:14px;line-height:22px;color:#554342}.bio-placeholder{color:#b7a39e}.avatar{display:flex;padding:12px 0 16px;align-items:center;border-top:1px solid rgba(202,173,164,.22)}.avatar>image{width:62px;height:62px;margin-right:12px;border-radius:50%}.avatar>view{display:flex;flex:1;flex-direction:column}.avatar-name{font-size:16px;font-weight:700}.album{display:flex;flex-wrap:wrap;justify-content:space-between}.photo,.add{position:relative;display:flex;box-sizing:border-box;width:31.5%;height:108px;margin-bottom:10px;overflow:hidden;border-radius:16px}.photo image{width:100%;height:100%}.delete{position:absolute;display:flex;right:6px;top:6px;width:24px;height:24px;border-radius:50%;align-items:center;justify-content:center;color:#fff;background:rgba(49,35,35,.62)}.add{flex-direction:column;align-items:center;justify-content:center;border:1px dashed rgba(196,92,92,.38);background:#fbf4ef;font-size:12px;color:#8b6f6a}.plus{font-size:28px;color:#c45c5c}.status-card{display:flex;min-height:120px;align-items:center;justify-content:center;flex-direction:column}.retry{margin-top:14px;color:#c45c5c;font-weight:700}.preview{display:flex;flex-direction:column;align-items:stretch}.preview .preview-cover{width:100%;height:320px;margin-bottom:14px;border-radius:18px}.preview-title-row{display:flex;align-items:center}.preview-title-row>image{width:58px;height:58px;margin:0 12px 0 0;border-radius:50%}.preview-title-row>view{display:flex;min-width:0;flex:1;flex-direction:column}.preview-bio{margin-top:14px;font-size:14px;line-height:22px;color:#785f5a}.tag-list{display:flex;flex-wrap:wrap;margin-top:10px}.tag{margin:0 8px 8px 0;padding:6px 10px;border-radius:16px;background:#fff2e7;font-size:12px;color:#8f675d}.preference-tag{background:#ffe9ef;color:#b35467}.preview-subsection{padding-top:16px;margin-top:8px;border-top:1px solid rgba(202,173,164,.22)}.preview-subtitle{font-size:16px;font-weight:700;color:#523c3d}.preview-album{display:flex;flex-wrap:wrap;justify-content:space-between;margin-top:14px}.preview-album image{width:31.5%;height:108px;margin-bottom:10px;border-radius:16px}.save-bar{position:absolute;z-index:30;left:0;right:0;bottom:0;padding:12px 16px calc(20px + env(safe-area-inset-bottom));background:linear-gradient(180deg,transparent,rgba(247,244,239,.96) 32%,#f7f4ef)}.save{display:flex;height:50px;border-radius:25px;align-items:center;justify-content:center;background:#c45c5c;color:#fff;font-size:16px;font-weight:700;box-shadow:0 12px 26px rgba(196,92,92,.18)}
</style>
<style>
  /* 资料编辑页沿用既有交互，只覆盖视觉令牌。 */
  .page { background: var(--marriage-soft); color: var(--marriage-text); }
  .header-bar { border: 0; border-radius: 24rpx; background: rgba(255, 255, 255, .96); box-shadow: var(--marriage-shadow); }
  .header-title, .title, .avatar-name { color: var(--marriage-text); }
  .mine-link, .auth-link, .plus { color: var(--marriage-primary); }
  .back-arrow { border-color: var(--marriage-muted); }
  .card { border: 0; border-radius: 24rpx; background: var(--marriage-surface); box-shadow: var(--marriage-shadow); }
  .description, .meta, .tabs view, .value, .bio-placeholder { color: var(--marriage-muted); }
  .auth { background: var(--marriage-mint); }
  .auth image { background: var(--marriage-sky); }
  .tabs .active, .save { background: var(--marriage-primary); }
  .row, .avatar { border-color: var(--marriage-line); }
  .arrow { border-color: var(--marriage-primary); }
  .bio, .add { background: var(--marriage-soft); }
  .add { border-color: #ffcaca; }
  .save-bar { background: linear-gradient(180deg, transparent, rgba(247, 249, 255, .96) 32%, var(--marriage-soft)); }
  .save { box-shadow: 0 8rpx 18rpx rgba(255, 107, 107, .18); }
</style>
