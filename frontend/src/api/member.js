import request from '@/utils/request'

export function getMemberList() {
  return request({
    url: '/members',
    method: 'get'
  })
}

export function searchMembers(keyword) {
  return request({
    url: '/members/search',
    method: 'get',
    params: { keyword }
  })
}

export function getMemberById(id) {
  return request({
    url: `/members/${id}`,
    method: 'get'
  })
}

export function getMemberByPhone(phone) {
  return request({
    url: `/members/phone/${phone}`,
    method: 'get'
  })
}

export function createMember(data) {
  return request({
    url: '/members',
    method: 'post',
    data
  })
}

export function updateMember(id, data) {
  return request({
    url: `/members/${id}`,
    method: 'put',
    data
  })
}

export function deleteMember(id) {
  return request({
    url: `/members/${id}`,
    method: 'delete'
  })
}

export function getPointsHistory(id) {
  return request({
    url: `/members/${id}/points-history`,
    method: 'get'
  })
}
