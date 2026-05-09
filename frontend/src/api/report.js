import request from '@/utils/request'

export function getDailyReport(date) {
  return request({
    url: '/reports/daily',
    method: 'get',
    params: date ? { date } : {}
  })
}

export function getWeeklyReport() {
  return request({
    url: '/reports/weekly',
    method: 'get'
  })
}
