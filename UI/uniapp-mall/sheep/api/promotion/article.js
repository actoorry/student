import request from '@/sheep/request';

export default {
    // 获得文章分页
    getArticlePage: (params) => {
        return request({
            url: '/sales/promotion/article/page',
            method: 'GET',
            params
        });
    },
    // 获得文章详情
    getArticle: (id, title) => {
        return request({
            url: '/sales/promotion/article/get',
            method: 'GET',
            params: { id, title }
        });
    }
}
