import request from '@/utils/request'

export function getMovieList() {
  return request({
    url: '/movies',
    method: 'get'
  })
}

export function searchMovies(keyword) {
  return request({
    url: '/movies/search',
    method: 'get',
    params: { keyword }
  })
}

export function getMovieById(id) {
  return request({
    url: `/movies/${id}`,
    method: 'get'
  })
}

export function createMovie(data) {
  return request({
    url: '/movies',
    method: 'post',
    data
  })
}

export function updateMovie(id, data) {
  return request({
    url: `/movies/${id}`,
    method: 'put',
    data
  })
}

export function deleteMovie(id) {
  return request({
    url: `/movies/${id}`,
    method: 'delete'
  })
}
