import { httpClient } from './httpClient';
import {
  ApiResponse,
  MenuItem,
  MenuItemCreateRequest,
  MenuItemUpdateRequest,
  MenuItemResponse,
  PaginatedResponse
} from '../types/api';

// Menu API Service - ONLY IMPLEMENTED ENDPOINTS
export class MenuApiService {
  
  // Menu Item endpoints (matching backend MenuController)
  async createMenuItem(branchId: number, menuItemData: MenuItemCreateRequest): Promise<ApiResponse<MenuItemResponse>> {
    const response = await httpClient.post(`/menu-items/branches/${branchId}`, menuItemData);
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
    const response = await httpClient.put(`/menu-items/${menuItemId}`, menuItemData);
    return {
      data: response.data,
      success: true,
      status: response.status,
    };
  }

  async deleteMenuItem(menuItemId: number): Promise<ApiResponse<void>> {
    const response = await httpClient.delete(`/menu-items/${menuItemId}`);
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

  async uploadMenuItemImage(
    menuItemId: number,
    file: { uri: string; name: string; type: string }
  ) {
    // NOT IMPLEMENTED - This endpoint doesn't exist in backend yet
    throw new Error('Menu item image upload not implemented in backend yet');
  }

  async deleteMenuItemImage(menuItemId: number): Promise<ApiResponse<void>> {
    // NOT IMPLEMENTED - This endpoint doesn't exist in backend yet
    throw new Error('Menu item image deletion not implemented in backend yet');
  }
}

// Export singleton instance
export const menuApiService = new MenuApiService();
export default menuApiService;