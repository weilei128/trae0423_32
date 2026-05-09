import request from '@/utils/request'

export function getOrderList() {
  return request({
    url: '/orders',
    method: 'get'
  })
}

export function getOrderById(id) {
  return request({
    url: `/orders/${id}`,
    method: 'get'
  })
}

export function getOrderByOrderNo(orderNo) {
  return request({
    url: `/orders/orderNo/${orderNo}`,
    method: 'get'
  })
}

export function createOrder(data) {
  return request({
    url: '/orders',
    method: 'post',
    data
  })
}

export function payOrder(id) {
  return request({
    url: `/orders/${id}/pay`,
    method: 'post'
  })
}

export function verifyOrder(qrCode) {
  return request({
    url: '/orders/verify',
    method: 'post',
    data: { qrCode }
  })
}

export function getOrdersByPhone(phone) {
  return request({
    url: `/orders/phone/${phone}`,
    method: 'get'
  })
}
