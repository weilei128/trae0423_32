import request from '@/utils/request'

export function getHallList() {
  return request({
    url: '/halls',
    method: 'get'
  })
}

export function getHallsByType(type) {
  return request({
    url: `/halls/type/${type}`,
    method: 'get'
  })
}

export function getHallById(id) {
  return request({
    url: `/halls/${id}`,
    method: 'get'
  })
}

export function createHall(data) {
  return request({
    url: '/halls',
    method: 'post',
    data
  })
}

export function updateHall(id, data) {
  return request({
    url: `/halls/${id}`,
    method: 'put',
    data
  })
}

export function deleteHall(id) {
  return request({
    url: `/halls/${id}`,
    method: 'delete'
  })
}
