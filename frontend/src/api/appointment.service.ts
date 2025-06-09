import { Appointment, AppointmentCreateDto, AppointmentDetails } from '@/types';
import axiosInstance from './axios';

class AppointmentService {
  async getUserAppointments(): Promise<AppointmentDetails[]> {
    const response = await axiosInstance.get('/appointments/user');
    return response.data;
  }

  async getAdvisorAppointments(): Promise<Appointment[]> {
    const response = await axiosInstance.get('/appointments/advisor');
    return response.data;
  }

  async getAppointmentById(id: number): Promise<AppointmentDetails> {
    const response = await axiosInstance.get(`/appointments/${id}`);
    return response.data;
  }

  async createAppointment(dto: AppointmentCreateDto): Promise<Appointment> {
    const response = await axiosInstance.post('/appointments', dto);
    return response.data;
  }

  async updateAppointmentStatus(id: number, status: string): Promise<AppointmentDetails> {
    const response = await axiosInstance.put(`/appointments/${id}/status?status=${status}`);
    return response.data;
  }

  async addAdvisorNotes(id: number, notes: string): Promise<Appointment> {
    const response = await axiosInstance.put(`/appointments/${id}/advisor-notes`, notes);
    return response.data;
  }
}

export default new AppointmentService();