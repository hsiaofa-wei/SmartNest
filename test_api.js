// 测试前端API调用
const axios = require('axios');

async function testSearchHouses() {
    try {
        // 测试直接调用后端API
        const backendRes = await axios.get('http://localhost:8080/houses/search', {
            params: {
                province: '北京市',
                city: '',
                page: 0,
                size: 10
            }
        });
        console.log('后端API直接调用结果:', backendRes.data);
        
        // 测试通过前端代理调用
        const frontendRes = await axios.get('http://localhost:3001/api/houses/search', {
            params: {
                province: '北京市',
                city: '',
                page: 0,
                size: 10
            }
        });
        console.log('前端代理调用结果:', frontendRes.data);
        
        // 测试没有city参数的情况
        const noCityRes = await axios.get('http://localhost:3001/api/houses/search', {
            params: {
                province: '北京市',
                page: 0,
                size: 10
            }
        });
        console.log('没有city参数的调用结果:', noCityRes.data);
        
    } catch (error) {
        console.error('测试失败:', error.message);
        if (error.response) {
            console.error('响应数据:', error.response.data);
        }
    }
}

testSearchHouses();