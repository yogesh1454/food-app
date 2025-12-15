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
    try {
      console.log('Creating vendor with data:', JSON.stringify(vendorData, null, 2));

      // Get or create UUID using Firestore-backed service (persists across app reinstalls)
      const { getUserUUID } = await import('../utils/userUuidService');
      const userId = await getUserUUID();
      console.log('[Vendor] Using UUID from Firestore:', userId);

      const response = await httpClient.post('/vendors', vendorData, {
        headers: {
          'X-User-Id': userId
        }
      });
      return {
        data: response.data,
        success: true,
        status: response.status,
      };
    } catch (error: any) {
      console.error('Create Vendor Error:', error);
      if (error.response) {
        console.error('Error Data:', JSON.stringify(error.response.data, null, 2));
        console.error('Error Status:', error.response.status);
      }
      throw error;
    }
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
  // Note: Backend checks ownership via X-User-Id (same ID used during vendor creation)
  async createBranch(vendorId: number, branchData: any): Promise<ApiResponse<Branch>> {
    console.log('[Branch] Creating branch for vendorId:', vendorId);
    console.log('[Branch] Branch data:', JSON.stringify(branchData, null, 2));

    // Get UUID using Firestore-backed service (persists across app reinstalls)
    const { getUserUUID } = await import('../utils/userUuidService');
    const userId = await getUserUUID();
    console.log('[Branch] Using X-User-Id:', userId);

    try {
      // Backend requires X-User-Id to verify ownership of the vendor
      const response = await httpClient.post(`/vendors/${vendorId}/branches`, branchData, {
        headers: {
          'X-User-Id': userId
        }
      });
      console.log('[Branch] Branch created successfully:', response.data);
      return {
        data: response.data,
        success: true,
        status: response.status,
      };
    } catch (error: any) {
      console.error('[Branch] Error creating branch:', {
        status: error?.response?.status,
        statusText: error?.response?.statusText,
        data: error?.response?.data,
        message: error?.message
      });
      throw error;
    }
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
    console.log('[Branch] Toggling branch status for branchId:', branchId, 'isOpen:', statusData.isOpen);

    // Get UUID using Firestore-backed service (persists across app reinstalls)
    const { getUserUUID } = await import('../utils/userUuidService');
    const userId = await getUserUUID();
    console.log('[Branch] Using X-User-Id:', userId);

    try {
      const response = await httpClient.put(`/branches/${branchId}/status`, statusData, {
        headers: {
          'X-User-Id': userId
        }
      });
      console.log('[Branch] Branch status toggled successfully:', response.data);
      return {
        data: response.data,
        success: true,
        status: response.status,
      };
    } catch (error: any) {
      console.error('[Branch] Error toggling branch status:', {
        status: error?.response?.status,
        statusText: error?.response?.statusText,
        data: error?.response?.data,
        message: error?.message
      });
      throw error;
    }
  }

  // ============================================================================
  // DEPRECATED METHODS - These endpoints do NOT exist in the Swagger spec
  // They should be removed in a future update
  // ============================================================================

  /**
   * @deprecated This endpoint doesn't exist in the backend API.
   * Operating hours should be managed via updateBranch() using the operatingHours JSON field.
   */
  async updateOperatingHours(branchId: number, hoursData: any): Promise<ApiResponse<Branch>> {
    console.warn('[DEPRECATED] updateOperatingHours: This endpoint does not exist in backend. Use updateBranch() instead.');
    throw new Error('This endpoint is deprecated and does not exist in the backend API. Use updateBranch() with operatingHours field instead.');
  }

  /**
   * @deprecated This endpoint doesn't exist in the backend API.
   * Operating hours can be retrieved via getBranch().
   */
  async getOperatingHours(branchId: number): Promise<ApiResponse<any>> {
    console.warn('[DEPRECATED] getOperatingHours: This endpoint does not exist in backend. Use getBranch() instead.');
    throw new Error('This endpoint is deprecated and does not exist in the backend API. Use getBranch() to get operating hours.');
  }

  /**
   * @deprecated This endpoint doesn't exist in the backend API.
   * Branch availability can be determined from getBranch() response (isOpen, isActive fields).
   */
  async checkBranchAvailability(branchId: number): Promise<ApiResponse<any>> {
    console.warn('[DEPRECATED] checkBranchAvailability: This endpoint does not exist in backend. Use getBranch() instead.');
    throw new Error('This endpoint is deprecated and does not exist in the backend API. Use getBranch() to check availability.');
  }

  /**
   * @deprecated This endpoint doesn't exist in the backend API.
   * Documents should be uploaded via uploadVendorFile() with target=branch and appropriate fileType.
   */
  async uploadDocument(branchId: number, documentData: DocumentUploadRequest): Promise<ApiResponse<DocumentResponse>> {
    console.warn('[DEPRECATED] uploadDocument: This endpoint does not exist in backend. Use uploadVendorFile() instead.');
    throw new Error('This endpoint is deprecated and does not exist in the backend API. Use uploadVendorFile() with target=branch.');
  }

  /**
   * @deprecated This endpoint doesn't exist in the backend API.
   * Document information is returned as part of the branch response in getBranch().
   */
  async getBranchDocuments(branchId: number): Promise<ApiResponse<DocumentResponse[]>> {
    console.warn('[DEPRECATED] getBranchDocuments: This endpoint does not exist in backend. Document info is in getBranch() response.');
    throw new Error('This endpoint is deprecated and does not exist in the backend API.');
  }

  /**
   * @deprecated This endpoint doesn't exist in the backend API.
   * To list branches for a vendor, you'll need to maintain this client-side or request backend implementation.
   */
  async getVendorBranches(vendorId: number): Promise<ApiResponse<Branch[]>> {
    console.warn('[DEPRECATED] getVendorBranches: This endpoint does not exist in backend yet.');
    throw new Error('Get vendor branches endpoint not implemented in backend yet');
  }

  /**
   * @deprecated This endpoint doesn't exist in the backend API.
   */
  async deleteDocument(documentId: number): Promise<ApiResponse<void>> {
    console.warn('[DEPRECATED] deleteDocument: This endpoint does not exist in backend yet.');
    throw new Error('Delete document endpoint not implemented in backend yet');
  }

  /**
   * @deprecated General image upload not implemented.
   * Use uploadVendorFile() with appropriate target and fileType.
   */
  async uploadImage(file: { uri: string; name: string; type: string }, category: string = 'menu') {
    console.warn('[DEPRECATED] uploadImage: Use uploadVendorFile() instead.');
    throw new Error('General image upload not implemented in backend yet');
  }

  /**
   * @deprecated General document upload not implemented.
   * Use uploadVendorFile() with appropriate target and fileType.
   */
  async uploadDocumentFile(file: { uri: string; name: string; type: string }, documentType: string) {
    console.warn('[DEPRECATED] uploadDocumentFile: Use uploadVendorFile() instead.');
    throw new Error('General document upload not implemented in backend yet');
  }
}

// Export singleton instance
export const vendorApiService = new VendorApiService();
export default vendorApiService;