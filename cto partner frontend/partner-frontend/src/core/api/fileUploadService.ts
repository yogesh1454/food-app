import * as ImagePicker from 'expo-image-picker';
import * as DocumentPicker from 'expo-document-picker';
import AsyncStorage from '@react-native-async-storage/async-storage';

export interface UploadResult {
  success: boolean;
  uri?: string;
  fileName?: string;
  fileType?: string;
  error?: string;
}

class FileUploadService {
  /**
   * Pick an image from photo library
   */
  async pickImageFromLibrary(): Promise<UploadResult> {
    try {
      const permissionResult = await ImagePicker.requestMediaLibraryPermissionsAsync();
      
      if (!permissionResult.granted) {
        return {
          success: false,
          error: 'Gallery permission is required to select images. Please enable it in your device settings.'
        };
      }

      const result = await ImagePicker.launchImageLibraryAsync({
        mediaTypes: ImagePicker.MediaTypeOptions.Images,
        allowsEditing: true,
        aspect: [1, 1],
        quality: 0.8,
      });

      if (result.canceled) {
        return {
          success: false,
          error: 'Image selection was cancelled'
        };
      }

      const asset = result.assets[0];
      if (!asset || !asset.uri) {
        return {
          success: false,
          error: 'No image selected or invalid image data'
        };
      }

      return {
        success: true,
        uri: asset.uri,
        fileName: `image_${Date.now()}.jpg`,
        fileType: 'image/jpeg'
      };
    } catch (error: any) {
      console.error('Image picker error:', error);
      return {
        success: false,
        error: error.message || 'Failed to pick image from library'
      };
    }
  }

  /**
   * Take a photo using camera
   */
  async takePhoto(): Promise<UploadResult> {
    try {
      const permissionResult = await ImagePicker.requestCameraPermissionsAsync();
      
      if (!permissionResult.granted) {
        return {
          success: false,
          error: 'Camera permission is required to take photos. Please enable it in your device settings.'
        };
      }

      const result = await ImagePicker.launchCameraAsync({
        allowsEditing: true,
        aspect: [1, 1],
        quality: 0.8,
      });

      if (result.canceled) {
        return {
          success: false,
          error: 'Photo capture was cancelled'
        };
      }

      const asset = result.assets[0];
      if (!asset || !asset.uri) {
        return {
          success: false,
          error: 'No photo captured or invalid photo data'
        };
      }

      return {
        success: true,
        uri: asset.uri,
        fileName: `camera_${Date.now()}.jpg`,
        fileType: 'image/jpeg'
      };
    } catch (error: any) {
      console.error('Camera picker error:', error);
      return {
        success: false,
        error: error.message || 'Failed to take photo'
      };
    }
  }

  /**
   * Pick a document (PDF, images, etc.)
   */
  async pickDocument(): Promise<UploadResult> {
    try {
      const result = await DocumentPicker.getDocumentAsync({
        type: ['application/pdf', 'image/*'],
        copyToCacheDirectory: true,
      });

      if (result.canceled) {
        return {
          success: false,
          error: 'Document selection was cancelled'
        };
      }

      const document = result.assets[0];
      if (!document || !document.uri || !document.name) {
        return {
          success: false,
          error: 'No document selected or invalid document data'
        };
      }

      return {
        success: true,
        uri: document.uri,
        fileName: document.name,
        fileType: document.mimeType || 'application/octet-stream'
      };
    } catch (error: any) {
      console.error('Document picker error:', error);
      return {
        success: false,
        error: error.message || 'Failed to pick document'
      };
    }
  }

  /**
   * Save image/document to local storage and return local URI
   * In a real app, this would upload to cloud storage
   */
  async saveToLocalStorage(uri: string, fileName: string): Promise<string> {
    try {
      // Generate a local storage key
      const storageKey = `local_file_${Date.now()}_${fileName}`;
      
      // In a real app, you would:
      // 1. Fetch the file from the URI
      // 2. Convert to base64 or copy to app's document directory
      // 3. Save to AsyncStorage or file system
      // 4. Return the local path

      // For mock implementation, just store the original URI with metadata
      const fileMetadata = {
        uri,
        fileName,
        timestamp: Date.now()
      };
      
      await AsyncStorage.setItem(storageKey, JSON.stringify(fileMetadata));
      
      // Return the local storage key as the "stored" URI
      return storageKey;
    } catch (error: any) {
      throw new Error(`Failed to save file: ${error.message}`);
    }
  }

  /**
   * Get file from local storage
   */
  async getFromLocalStorage(storageKey: string): Promise<any> {
    try {
      const fileMetadata = await AsyncStorage.getItem(storageKey);
      return fileMetadata ? JSON.parse(fileMetadata) : null;
    } catch (error: any) {
      throw new Error(`Failed to retrieve file: ${error.message}`);
    }
  }

  /**
   * Show image picker options (camera, gallery, cancel)
   */
  async pickImageWithOptions(source?: 'camera' | 'gallery'): Promise<UploadResult> {
    // This is a mock implementation - in real app you'd show a custom action sheet
    // For now, let user choose via prompt
    const choice = source || 'gallery'; // Default to gallery in mock
    
    if (choice === 'camera') {
      return await this.takePhoto();
    } else {
      return await this.pickImageFromLibrary();
    }
  }

  /**
   * Upload image with camera/gallery options
   */
  async uploadImage(): Promise<string | null> {
    const result = await this.pickImageWithOptions();
    
    if (result.success && result.uri) {
      try {
        const localUri = await this.saveToLocalStorage(result.uri, result.fileName || 'image.jpg');
        return localUri;
      } catch (error: any) {
        throw new Error(error.message);
      }
    } else {
      throw new Error(result.error || 'Failed to upload image');
    }
  }

  /**
   * Upload document
   */
  async uploadDocument(): Promise<string | null> {
    const result = await this.pickDocument();
    
    if (result.success && result.uri) {
      try {
        const localUri = await this.saveToLocalStorage(result.uri, result.fileName || 'document');
        return localUri;
      } catch (error: any) {
        throw new Error(error.message);
      }
    } else {
      throw new Error(result.error || 'Failed to upload document');
    }
  }
}

export default new FileUploadService();
