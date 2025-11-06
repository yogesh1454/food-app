import { useState } from 'react';
import fileUploadService from '../services/fileUploadService';

interface UseFileUploadReturn {
  uploading: boolean;
  uploadImage: () => Promise<string | null>;
  uploadDocument: () => Promise<string | null>;
  takePhoto: () => Promise<string | null>;
  pickFromGallery: () => Promise<string | null>;
}

export const useFileUpload = (): UseFileUploadReturn => {
  const [uploading, setUploading] = useState(false);

  const uploadImage = async (): Promise<string | null> => {
    setUploading(true);
    try {
      return await fileUploadService.uploadImage();
    } finally {
      setUploading(false);
    }
  };

  const uploadDocument = async (): Promise<string | null> => {
    setUploading(true);
    try {
      return await fileUploadService.uploadDocument();
    } finally {
      setUploading(false);
    }
  };

  const takePhoto = async (): Promise<string | null> => {
    setUploading(true);
    try {
      const result = await fileUploadService.takePhoto();
      if (result.success && result.uri) {
        return await fileUploadService.saveToLocalStorage(result.uri, result.fileName || 'photo.jpg');
      }
      return null;
    } finally {
      setUploading(false);
    }
  };

  const pickFromGallery = async (): Promise<string | null> => {
    setUploading(true);
    try {
      const result = await fileUploadService.pickImageFromLibrary();
      if (result.success && result.uri) {
        return await fileUploadService.saveToLocalStorage(result.uri, result.fileName || 'image.jpg');
      }
      return null;
    } finally {
      setUploading(false);
    }
  };

  return {
    uploading,
    uploadImage,
    uploadDocument,
    takePhoto,
    pickFromGallery,
  };
};

export default useFileUpload;
