import { useState } from 'react';
import MediaHelper from '../helpers/mediaHelper';
import FileUploadApi from '../api/fileUploadApi';

interface UseFileUploadReturn {
  uploading: boolean;
  uploadImage: (userId: string) => Promise<string | null>;
  uploadDocument: (userId: string, docType: string) => Promise<string | null>;
  takePhoto: (userId: string) => Promise<string | null>;
  pickFromGallery: (userId: string) => Promise<string | null>;
  error: string | null;
}

export const useFileUpload = (): UseFileUploadReturn => {
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleUpload = async (
    pickFn: () => Promise<any>,
    uploadFn: (uri: string) => Promise<any>
  ): Promise<string | null> => {
    setUploading(true);
    setError(null);
    try {
      const pickResult = await pickFn();

      if (!pickResult.success || !pickResult.uri) {
        if (pickResult.error) setError(pickResult.error);
        return null;
      }

      const uploadResult = await uploadFn(pickResult.uri);

      if (uploadResult.success) {
        return uploadResult.url || uploadResult.documentId || 'success';
      } else {
        setError(uploadResult.error || 'Upload failed');
        return null;
      }
    } catch (err: any) {
      setError(err.message || 'An unexpected error occurred');
      return null;
    } finally {
      setUploading(false);
    }
  };

  const uploadImage = async (userId: string): Promise<string | null> => {
    return handleUpload(
      () => MediaHelper.pickImageFromLibrary(),
      (uri) => FileUploadApi.uploadProfilePicture(userId, uri)
    );
  };

  const takePhoto = async (userId: string): Promise<string | null> => {
    return handleUpload(
      () => MediaHelper.takePhoto(),
      (uri) => FileUploadApi.uploadProfilePicture(userId, uri)
    );
  };

  const pickFromGallery = async (userId: string): Promise<string | null> => {
    return handleUpload(
      () => MediaHelper.pickImageFromLibrary(),
      (uri) => FileUploadApi.uploadProfilePicture(userId, uri)
    );
  };

  const uploadDocument = async (userId: string, docType: string): Promise<string | null> => {
    return handleUpload(
      () => MediaHelper.pickDocument(),
      (uri) => FileUploadApi.uploadDocument(userId, uri, docType)
    );
  };

  return {
    uploading,
    error,
    uploadImage,
    uploadDocument,
    takePhoto,
    pickFromGallery,
  };
};

export default useFileUpload;
