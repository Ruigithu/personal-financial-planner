import { PasswordChangeRequest, UserProfile, UserProfileUpdateDto } from '@/types';
import axiosInstance from './axios';

class UserService {
  async getUserProfile(): Promise<UserProfile> {
    const response = await axiosInstance.get('/user/profile');
    return response.data;
  }

  async updateUserProfile(profileUpdate: UserProfileUpdateDto): Promise<UserProfile> {
    const response = await axiosInstance.put('/user/profile', profileUpdate);
    return response.data;
  }

  async changePassword(passwordRequest: PasswordChangeRequest): Promise<any> {
    const response = await axiosInstance.put('/user/password', passwordRequest);
    return response.data;
  }
}

export default new UserService();