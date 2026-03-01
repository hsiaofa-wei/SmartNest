let isAMapLoaded = false;
let loadPromise = null;

// 高德地图配置（升级到v2.0版本）
const AMapConfig = {
  apiKey: '4b40b685be4aa782af3f17081feb259a',
  securityKey: '588b1c60ab738018bc54f40b2e63eb52',
  version: '2.0'
};

// 注意：全局错误处理已移至main.js中统一管理，此处不再重复定义
// 若需要添加新的错误处理逻辑，请在main.js中进行修改

export const mapService = {
  // 加载高德地图脚本
  loadAMapScript: function() {
    if (isAMapLoaded) {
      return Promise.resolve(window.AMap);
    }
    
    if (loadPromise) {
      return loadPromise;
    }
    
    loadPromise = new Promise((resolve, reject) => {
      // 检查是否已加载
      if (window.AMap) {
        console.log('检测到已加载的高德地图');
        isAMapLoaded = true;
        resolve(window.AMap);
        return;
      }
      
      // 设置安全密钥
      window._AMapSecurityConfig = {
        securityJsCode: AMapConfig.securityKey
      };
      
      const script = document.createElement('script');
      script.type = 'text/javascript';
      script.async = true;
      script.defer = true;
      // 高德地图v2.0版本的URL格式（包含安全密钥）
      script.src = `https://webapi.amap.com/maps?v=${AMapConfig.version}&key=${AMapConfig.apiKey}&securityJsCode=${AMapConfig.securityKey}`;
      
      script.onload = () => {
        console.log('高德地图动态加载成功');
        isAMapLoaded = true;
        resolve(window.AMap);
      };
      
      script.onerror = (error) => {
        // 优化错误处理，提供更详细的错误信息
        const errorInfo = error ? error.message || JSON.stringify(error) : '未知错误';
        console.error('高德地图动态加载失败:', error);
        reject(new Error(`地图脚本加载失败: ${errorInfo}`));
      };
      
      document.head.appendChild(script);
      
      // 设置超时
      setTimeout(() => {
        if (!isAMapLoaded) {
          reject(new Error('地图脚本加载超时'));
        }
      }, 10000);
    });
    
    return loadPromise;
  },
  
  // 创建地图实例
  createMap: function(container, options = {}) {
    return new Promise((resolve, reject) => {
      this.loadAMapScript()
        .then((AMap) => {
          try {
            // 确保容器存在
            const containerElement = typeof container === 'string' 
              ? document.getElementById(container) 
              : container;
              
            if (!containerElement) {
              throw new Error(`地图容器 ${container} 不存在`);
            }
            
            // 设置容器最小高度
            containerElement.style.minHeight = containerElement.style.minHeight || '300px';
            
            // 创建地图（使用高德地图v2.0版本的API）
        const map = new AMap.Map(containerElement, {
          center: options.center || [116.404, 39.915],
          zoom: options.zoom || 11,
          viewMode: options.viewMode || '2D',
          resizeEnable: true,
          ...options
        });
        
        // 设置缩放范围限制（3-20级），使用v2.0版本的API
        if (map.setZoomLimit) {
          map.setZoomLimit({
            min: 3,
            max: 20
          });
        } else if (map.setZoomMin && map.setZoomMax) {
          // 兼容旧版本API
          map.setZoomMin(3);
          map.setZoomMax(20);
        }
          
          resolve(map);
          } catch (error) {
            console.error('创建地图实例失败:', error);
            reject(error);
          }
        })
        .catch(reject);
    });
  },
  
  // 检查AMap是否可用
  isAMapAvailable: function() {
    return !!window.AMap;
  },
  
  // 重置加载状态
  reset: function() {
    isAMapLoaded = false;
    loadPromise = null;
  }
};
