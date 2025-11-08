import { useState } from 'react';
import { FileUploadService } from '../services/fileUploadService';

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
      const result = await FileUploadService.pickImage();
      return result ? result.uri : null;
    } finally {
      setUploading(false);
    }
  };

  const uploadDocument = async (): Promise<string | null> => {
    setUploading(true);
    try {
      const result = await FileUploadService.pickDocument();
      return result ? result.uri : null;
    } finally {
      setUploading(false);
    }
  };

  const takePhoto = async (): Promise<string | null> => {
    setUploading(true);
    try {
      const result = await FileUploadService.pickImage();
      return result ? result.uri : null;
    } finally {
      setUploading(false);
    }
  };

  const pickFromGallery = async (): Promise<string | null> => {
    setUploading(true);
    try {
      const result = await FileUploadService.pickImage();
      return result ? result.uri : null;
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
