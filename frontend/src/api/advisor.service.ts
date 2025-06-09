import { Advisor, AdvisorProfileDto, AvailabilitySlot, AvailabilitySlotDto, TimeSlot } from '../types';
import axiosInstance from './axios';

class AdvisorService {
  async getAllAdvisors(): Promise<Advisor[]> {
    const response = await axiosInstance.get('/advisors');
    return response.data;
  }

  async getAdvisorById(id: number): Promise<Advisor> {
    const response = await axiosInstance.get(`/advisors/${id}`);
    return response.data;
  }

  async getAdvisorsBySpecialty(specialty: string): Promise<Advisor[]> {
    const response = await axiosInstance.get(`/advisors/specialty/${specialty}`);
    return response.data;
  }

  async getAdvisorsByLanguage(language: string): Promise<Advisor[]> {
    const response = await axiosInstance.get(`/advisors/language/${language}`);
    return response.data;
  }

  async updateAdvisorProfile(profileDto: AdvisorProfileDto): Promise<Advisor> {
    const response = await axiosInstance.put('/advisors/profile', profileDto);
    return response.data;
  }

  async getMyAvailability(): Promise<AvailabilitySlot[]> {
    const response = await axiosInstance.get('/advisors/availability');
    return response.data;
  }

  async addAvailabilitySlot(slotDto: AvailabilitySlotDto): Promise<AvailabilitySlot> {
    const response = await axiosInstance.post('/advisors/availability', slotDto);
    return response.data;
  }

  async removeAvailabilitySlot(slotId: number): Promise<any> {
    const response = await axiosInstance.delete(`/advisors/availability/${slotId}`);
    return response.data;
  }

  async getAvailableTimeSlots(advisorId: number, startDate: string, endDate: string): Promise<TimeSlot[]> {
    const response = await axiosInstance.get(`/advisors/${advisorId}/available-slots`, {
      params: { startDate, endDate },
    });
    return response.data;
  }
}

export default new AdvisorService();