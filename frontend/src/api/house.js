import request from './request'

export const searchHouses = (params) => {
  return request({
    url: '/houses/search',
    method: 'get',
    params
  })
}

export const getHouseDetail = (id) => {
  return request({
    url: `/houses/public/${id}`,
    method: 'get'
  })
}

export const getRecommendedHouses = (params) => {
  return request({
    url: '/houses/recommended',
    method: 'get',
    params
  })
}

export const toggleFavorite = (houseId) => {
  return request({
    url: `/tenant/favorites/${houseId}`,
    method: 'post',
    params: {
      tenantId: JSON.parse(localStorage.getItem('user')).id
    }
  })
}

export const getFavorites = (params) => {
  return request({
    url: '/tenant/favorites',
    method: 'get',
    params: {
      tenantId: JSON.parse(localStorage.getItem('user')).id,
      ...params
    }
  })
}

