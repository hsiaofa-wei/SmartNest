import request from './request'

export const getCaptcha = () => {
  return request({
    url: '/auth/captcha',
    method: 'get'
  })
}

export const login = (data) => {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export const register = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export const sendVerificationCode = (data) => {
  return request({
    url: '/auth/sendVerificationCode',
    method: 'post',
    data
  })
}

