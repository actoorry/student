/**
 * 本地图片转 base64（帮扶申请附件上传用）
 * @param {string} src 图片地址
 * @return {Promise<string>} base64
 */
export function imageToBase64(src) {
	return new Promise((resolve, reject) => {
		uni.getImageInfo({
			src,
			success: (image) => {
				uni.getFileSystemManager().readFile({
					filePath: image.path,
					encoding: 'base64',
					success: (e) => resolve(`data:image/jpeg;base64,${e.data}`),
					fail: () => reject(null),
				})
			},
			fail: reject,
		})
	})
}
