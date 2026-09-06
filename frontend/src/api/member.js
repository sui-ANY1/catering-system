import request from './request'

export const getMemberList = () => request.get('/member')
export const getMember = (id) => request.get(`/member/${id}`)
export const addMember = (data) => request.post('/member', data)
export const updateMember = (data) => request.put('/member', data)
export const deleteMember = (id) => request.delete(`/member/${id}`)
