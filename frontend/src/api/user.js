import request from './request'

export const getProfile = () => {
  return request({
    url: '/user/profile',
    method: 'get'
  })
}

export const updateProfile = (data) => {
  return request({
    url: '/user/profile',
    method: 'put',
    data
  })
}

export const changePassword = (data) => {
  return request({
    url: '/user/password',
    method: 'put',
    data
  })
}

export const uploadFile = (file, userId) => {
  const formData = new FormData()
  formData.append('file', file)
  if (userId) {
    formData.append('userId', userId)
  }
  return request({
    url: '/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

