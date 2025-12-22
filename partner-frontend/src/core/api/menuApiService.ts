import { httpClient } from './httpClient';
import {
  ApiResponse,
  MenuItem,
  MenuItemCreateRequest,
  MenuItemUpdateRequest,
  MenuItemResponse,
  PaginatedResponse,
  ImageUploadResponse
} from '../types/api';
import { getUserUUID } from '../utils/userUuidService';

// Menu API Service - ONLY IMPLEMENTED ENDPOINTS
export class MenuApiService {

  // Menu Item endpoints (matching backend MenuController)
  async createMenuItem(branchId: number, menuItemData: MenuItemCreateRequest): Promise<ApiResponse<MenuItemResponse>> {
    console.log('[Menu] Creating menu item for branchId:', branchId);
    const userId = await getUserUUID();
    console.log('[Menu] Using X-User-Id:', userId);

    const response = await httpClient.post(`/menu-items/branches/${branchId}`, menuItemData, {
      headers: {
        'X-User-Id': userId
      }
    });
    console.log('[Menu] Menu item created:', response.data);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async getMenuItem(menuItemId: number): Promise<ApiResponse<MenuItemResponse>> {
    const response = await httpClient.get(`/menu-items/${menuItemId}`);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async updateMenuItem(menuItemId: number, menuItemData: MenuItemUpdateRequest): Promise<ApiResponse<MenuItemResponse>> {
    console.log('[Menu] Updating menu item:', menuItemId);
    const userId = await getUserUUID();

    const response = await httpClient.put(`/menu-items/${menuItemId}`, menuItemData, {
      headers: {
        'X-User-Id': userId
      }
    });
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async deleteMenuItem(menuItemId: number): Promise<ApiResponse<void>> {
    console.log('[Menu] Deleting menu item:', menuItemId);
    const userId = await getUserUUID();

    const response = await httpClient.delete(`/menu-items/${menuItemId}`, {
      headers: {
        'X-User-Id': userId
      }
    });
    return {
      data: undefined,
      success: true,
      status: response.status,
    };
  }

  async getBranchMenuItems(
    branchId: number,
    page: number = 0,
    size: number = 50,
    category?: string
  ): Promise<ApiResponse<MenuItemResponse[]>> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });

    if (category) {
      params.append('category', category);
    }

    const response = await httpClient.get(`/menu-items/branches/${branchId}?${params.toString()}`);
    return {
      data: response.data, // Backend returns List<MenuItemResponse>, not paginated
      success: true,
      status: response.status,
    };
  }

  // NOTE: The following endpoints are NOT implemented in backend yet
  // These are placeholders for future implementation

  async searchMenuItems(
    branchId: number,
    query: string,
    page: number = 0,
    size: number = 20
  ): Promise<ApiResponse<MenuItemResponse[]>> {
    // NOT IMPLEMENTED - This endpoint doesn't exist in backend yet
    throw new Error('Search functionality not implemented in backend yet');
  }

  async getMenuItemCategories(branchId: number): Promise<ApiResponse<string[]>> {
    // NOT IMPLEMENTED - This endpoint doesn't exist in backend yet
    throw new Error('Categories endpoint not implemented in backend yet');
  }

  async bulkUpdateMenuItemAvailability(
    menuItemIds: number[],
    isAvailable: boolean
  ): Promise<ApiResponse<MenuItemResponse[]>> {
    // NOT IMPLEMENTED - This endpoint doesn't exist in backend yet
    throw new Error('Bulk update functionality not implemented in backend yet');
  }

  async duplicateMenuItem(menuItemId: number, newName?: string): Promise<ApiResponse<MenuItemResponse>> {
    // NOT IMPLEMENTED - This endpoint doesn't exist in backend yet
    throw new Error('Duplicate functionality not implemented in backend yet');
  }

  async getPopularMenuItems(
    branchId: number,
    period: 'week' | 'month' | 'quarter' = 'month',
    limit: number = 10
  ): Promise<ApiResponse<MenuItemResponse[]>> {
    // NOT IMPLEMENTED - This endpoint doesn't exist in backend yet
    throw new Error('Popular items endpoint not implemented in backend yet');
  }

  async getMenuAnalytics(branchId: number, period: 'week' | 'month' | 'quarter' = 'month') {
    // NOT IMPLEMENTED - This endpoint doesn't exist in backend yet
    throw new Error('Menu analytics endpoint not implemented in backend yet');
  }

  /**
   * Upload image for a menu item
   * POST /api/v1/menu-items/{menuItemId}/images
   * @param menuItemId - The menu item ID
   * @param imageUri - Local file URI of the image
   * @param imageType - 'primary' for main image, 'gallery' for additional images
   */
  async uploadMenuItemImage(
    menuItemId: number,
    imageUri: string,
    imageType: 'primary' | 'gallery' = 'primary'
  ): Promise<ApiResponse<ImageUploadResponse>> {
    console.log('[Menu] Uploading image for menuItemId:', menuItemId, 'type:', imageType);
    const userId = await getUserUUID();

    // Create form data for multipart upload
    const formData = new FormData();

    // Extract filename from URI
    const filename = imageUri.split('/').pop() || 'image.jpg';
    const fileExtension = filename.split('.').pop()?.toLowerCase() || 'jpg';

    // Determine MIME type
    const mimeTypes: { [key: string]: string } = {
      'jpg': 'image/jpeg',
      'jpeg': 'image/jpeg',
      'png': 'image/png',
      'gif': 'image/gif',
      'webp': 'image/webp'
    };
    const mimeType = mimeTypes[fileExtension] || 'image/jpeg';

    // Append file to form data
    formData.append('file', {
      uri: imageUri,
      name: filename,
      type: mimeType
    } as any);

    try {
      const response = await httpClient.post(
        `/menu-items/${menuItemId}/images?imageType=${imageType}`,
        formData,
        {
          headers: {
            'X-User-Id': userId,
            'Content-Type': 'multipart/form-data',
          },
        }
      );

      console.log('[Menu] Image uploaded successfully:', response.data);
      return {
        data: response.data,
        success: true,
        status: response.status,
      };
    } catch (error: any) {
      console.error('[Menu] Error uploading image:', {
        status: error?.response?.status,
        data: error?.response?.data,
        message: error?.message
      });
      throw error;
    }
  }

  async deleteMenuItemImage(menuItemId: number): Promise<ApiResponse<void>> {
    // NOT IMPLEMENTED - This endpoint doesn't exist in backend yet
    throw new Error('Menu item image deletion not implemented in backend yet');
  }
}

// Export singleton instance
export const menuApiService = new MenuApiService();
export default menuApiService;