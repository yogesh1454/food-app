import * as ImagePicker from 'expo-image-picker';
import * as DocumentPicker from 'expo-document-picker';

export interface UploadResult {
    success: boolean;
    uri?: string;
    fileName?: string;
    fileType?: string;
    error?: string;
}

class MediaHelper {
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
     * Show image picker options (camera, gallery, cancel)
     */
    async pickImageWithOptions(source?: 'camera' | 'gallery'): Promise<UploadResult> {
        const choice = source || 'gallery';

        if (choice === 'camera') {
            return await this.takePhoto();
        } else {
            return await this.pickImageFromLibrary();
        }
    }
}

export default new MediaHelper();
