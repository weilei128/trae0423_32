import request from '@/utils/request'

export function getScheduleList() {
  return request({
    url: '/schedules',
    method: 'get'
  })
}

export function getSchedulesByMovie(movieId) {
  return request({
    url: `/schedules/movie/${movieId}`,
    method: 'get'
  })
}

export function getSchedulesByHall(hallId) {
  return request({
    url: `/schedules/hall/${hallId}`,
    method: 'get'
  })
}

export function getScheduleById(id) {
  return request({
    url: `/schedules/${id}`,
    method: 'get'
  })
}

export function getBookedSeats(id) {
  return request({
    url: `/schedules/${id}/booked-seats`,
    method: 'get'
  })
}

export function createSchedule(data) {
  return request({
    url: '/schedules',
    method: 'post',
    data
  })
}

export function updateSchedule(id, data) {
  return request({
    url: `/schedules/${id}`,
    method: 'put',
    data
  })
}

export function deleteSchedule(id) {
  return request({
    url: `/schedules/${id}`,
    method: 'delete'
  })
}
