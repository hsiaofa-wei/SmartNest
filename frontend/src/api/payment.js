import request from './request'

export const createOrder = (data) => {
  return request({
    url: '/tenant/orders',
    method: 'post',
    data
  })
}

export const createAlipayOrder = (orderId) => {
  return request({
    url: `/payment/${orderId}/alipay`,
    method: 'post'
  })
}

export const createAlipayQrCode = (orderId) => {
  return request({
    url: `/payment/${orderId}/alipay/qr`,
    method: 'post'
  })
}

