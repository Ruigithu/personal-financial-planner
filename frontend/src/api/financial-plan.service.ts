
import { FinancialPlan, FinancialPlanRequest } from '@/types';
import axiosInstance from './axios';

class FinancialPlanService {
  async getUserPlans(): Promise<FinancialPlan[]> {
    const response = await axiosInstance.get('/plans');
    console.log(response.data);
    return response.data;
  }

  async getPlanById(id: number): Promise<FinancialPlan> {
    const response = await axiosInstance.get(`/plans/${id}`);
    return response.data;
  }

  async getPlanByShareCode(shareCode: string): Promise<FinancialPlan> {
    const response = await axiosInstance.get(`/plans/shared/${shareCode}`);
    return response.data;
  }

  async generatePlan(request: FinancialPlanRequest): Promise<FinancialPlan> {
    const response = await axiosInstance.post('/plans', request);
    return response.data;
  }

  async downloadPlanPdf(id: number): Promise<Blob> {
    const response = await axiosInstance.get(`/plans/${id}/pdf`, {
      responseType: 'blob',
    });
    return response.data;
  }
}

export default new FinancialPlanService();