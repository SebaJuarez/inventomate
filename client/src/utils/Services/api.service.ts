import axios, { AxiosRequestConfig, AxiosError } from 'axios';
import { handleApiError } from '../errorHander';
 
export const url = "http://34.148.152.120/"

export class ApiService {


  async request<T>(
    accessToken: string,
    endpoint: string,
    method: 'GET' | 'POST' | 'PUT' | 'DELETE',
    data?: any
  ): Promise<T> {
    const config: AxiosRequestConfig = {
      url: `${url}/api${endpoint}`,
      method,
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      data,
    };

    try {
      const response = await axios(config);
      return response.data;
    } catch (error: any) {
      const apiError = handleApiError(error as AxiosError);
      throw apiError;
    }
  }
}