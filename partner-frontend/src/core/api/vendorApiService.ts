import { httpClient } from './httpClient';
import {
  ApiResponse,
  Vendor,
  Branch,
  DocumentUploadRequest,
  DocumentResponse,
} from '../types/api';

// Vendor API Service - ONLY IMPLEMENTED ENDPOINTS
export class VendorApiService {

  // Vendor endpoints (matching backend VendorController)
  async getVendor(vendorId: number): Promise<ApiResponse<Vendor>> {
    const response = await httpClient.get(`/vendors/${vendorId}`);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async createVendor(vendorData: any): Promise<ApiResponse<Vendor>> {
    const response = await httpClient.post('/vendors', vendorData);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async updateVendor(vendorId: number, vendorData: any): Promise<ApiResponse<Vendor>> {
    const response = await httpClient.put(`/vendors/${vendorId}`, vendorData);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  // File upload endpoint (matching backend VendorController)
  async uploadVendorFile(
    vendorId: number,
    file: { uri: string; name: string; type: string },
    target: string,
    fileType: string,
    additionalData?: Record<string, string>
  ): Promise<ApiResponse<any>> {
    const formData = new FormData();
    formData.append('file', {
      uri: file.uri,
      name: file.name,
      type: file.type,
    } as any);

    // Add required parameters
    formData.append('target', target);
    formData.append('fileType', fileType);

    // Add additional data if provided
    if (additionalData) {
      Object.entries(additionalData).forEach(([key, value]) => {
        formData.append(key, value);
      });
    }

    const response = await httpClient.post(`/vendors/${vendorId}/upload`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  // NOTE: The following endpoints are NOT implemented in backend yet
  // These are placeholders for future implementation

  // Branch endpoints (matching backend BranchController)
  async createBranch(vendorId: number, branchData: any): Promise<ApiResponse<Branch>> {
    const response = await httpClient.post(`/vendors/${vendorId}/branches`, branchData);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async getBranch(branchId: number): Promise<ApiResponse<Branch>> {
    const response = await httpClient.get(`/branches/${branchId}`);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async updateBranch(vendorId: number, branchId: number, branchData: any): Promise<ApiResponse<Branch>> {
    const response = await httpClient.put(`/vendors/${vendorId}/branches/${branchId}`, branchData);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async toggleBranchStatus(branchId: number, statusData: { isOpen: boolean }): Promise<ApiResponse<Branch>> {
    const response = await httpClient.put(`/branches/${branchId}/status`, statusData);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async updateOperatingHours(branchId: number, hoursData: any): Promise<ApiResponse<Branch>> {
    const response = await httpClient.put(`/branches/${branchId}/operating-hours`, hoursData);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async getOperatingHours(branchId: number): Promise<ApiResponse<any>> {
    const response = await httpClient.get(`/branches/${branchId}/operating-hours`);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async checkBranchAvailability(branchId: number): Promise<ApiResponse<any>> {
    const response = await httpClient.get(`/branches/${branchId}/availability`);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async uploadDocument(branchId: number, documentData: DocumentUploadRequest): Promise<ApiResponse<DocumentResponse>> {
    const response = await httpClient.post(`/branches/${branchId}/documents`, documentData);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async getBranchDocuments(branchId: number): Promise<ApiResponse<DocumentResponse[]>> {
    const response = await httpClient.get(`/branches/${branchId}/documents`);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  // NOTE: The following endpoints are NOT implemented in backend yet
  // These are placeholders for future implementation

  async getVendorBranches(vendorId: number): Promise<ApiResponse<Branch[]>> {
    // NOT IMPLEMENTED - This endpoint doesn't exist in backend yet
    throw new Error('Get vendor branches endpoint not implemented in backend yet');
  }

  async deleteDocument(documentId: number): Promise<ApiResponse<void>> {
    // NOT IMPLEMENTED - This endpoint doesn't exist in backend yet
    throw new Error('Delete document endpoint not implemented in backend yet');
  }

  async uploadImage(file: { uri: string; name: string; type: string }, category: string = 'menu') {
    // NOT IMPLEMENTED - General image upload not implemented in backend yet
    throw new Error('General image upload not implemented in backend yet');
  }

  async uploadDocumentFile(file: { uri: string; name: string; type: string }, documentType: string) {
    // NOT IMPLEMENTED - General document upload not implemented in backend yet
    throw new Error('General document upload not implemented in backend yet');
  }
}

// Export singleton instance
export const vendorApiService = new VendorApiService();
export default vendorApiService;