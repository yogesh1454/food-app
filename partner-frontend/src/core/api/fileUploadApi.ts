import AsyncStorage from '@react-native-async-storage/async-storage';

// Base URL should ideally come from environment variables or a config file
const API_BASE_URL = 'https://api.nashtto.com/api';

export interface FileUploadResponse {
    success: boolean;
    url?: string;
    documentId?: string;
    message?: string;
    error?: string;
}

class FileUploadApi {
    private async getAuthToken(): Promise<string | null> {
        try {
            // Assuming token is stored with this key, adjust if needed based on auth implementation
            return await AsyncStorage.getItem('userToken');
        } catch (error) {
            console.error('Error getting auth token:', error);
            return null;
        }
    }

    /**
     * Upload profile picture
     * POST /users/{userId}/profile/picture
     */
    async uploadProfilePicture(userId: string, fileUri: string): Promise<FileUploadResponse> {
        try {
            const token = await this.getAuthToken();
            if (!token) {
                return { success: false, error: 'Authentication required' };
            }

            const formData = new FormData();
            const fileName = fileUri.split('/').pop() || 'profile.jpg';
            const fileType = fileName.endsWith('.png') ? 'image/png' : 'image/jpeg';

            // Append file to FormData
            // Note: React Native FormData requires uri, name, and type
            formData.append('file', {
                uri: fileUri,
                name: fileName,
                type: fileType,
            } as any);

            const response = await fetch(`${API_BASE_URL}/users/${userId}/profile/picture`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'multipart/form-data',
                },
                body: formData,
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                return {
                    success: false,
                    error: errorData.message || `Upload failed with status ${response.status}`
                };
            }

            const data = await response.json();
            return {
                success: true,
                url: data.profile_picture_url,
                message: 'Profile picture uploaded successfully'
            };
        } catch (error: any) {
            console.error('Profile picture upload error:', error);
            return {
                success: false,
                error: error.message || 'Network error during upload'
            };
        }
    }

    /**
     * Upload generic document
     * This is a placeholder implementation until the specific endpoint is confirmed
     * It follows the pattern of the profile picture upload
     */
    async uploadDocument(userId: string, fileUri: string, documentType: string): Promise<FileUploadResponse> {
        try {
            const token = await this.getAuthToken();
            if (!token) {
                return { success: false, error: 'Authentication required' };
            }

            const formData = new FormData();
            const fileName = fileUri.split('/').pop() || 'document.pdf';

            // Determine mime type roughly
            let mimeType = 'application/octet-stream';
            if (fileName.endsWith('.pdf')) mimeType = 'application/pdf';
            else if (fileName.endsWith('.jpg') || fileName.endsWith('.jpeg')) mimeType = 'image/jpeg';
            else if (fileName.endsWith('.png')) mimeType = 'image/png';

            formData.append('file', {
                uri: fileUri,
                name: fileName,
                type: mimeType,
            } as any);

            formData.append('documentType', documentType);

            // Using a hypothetical endpoint based on BranchController patterns
            // In a real scenario, this URL would be dynamic based on context (e.g., branch ID)
            // For now, we'll assume a user-centric document upload or similar
            const response = await fetch(`${API_BASE_URL}/users/${userId}/documents`, {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'multipart/form-data',
                },
                body: formData,
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                return {
                    success: false,
                    error: errorData.message || `Upload failed with status ${response.status}`
                };
            }

            const data = await response.json();
            return {
                success: true,
                documentId: data.documentId,
                url: data.url,
                message: 'Document uploaded successfully'
            };
        } catch (error: any) {
            console.error('Document upload error:', error);
            return {
                success: false,
                error: error.message || 'Network error during upload'
            };
        }
    }
}

export default new FileUploadApi();
