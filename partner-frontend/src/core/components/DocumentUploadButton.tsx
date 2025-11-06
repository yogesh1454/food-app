import React from 'react';
import {
  TouchableOpacity,
  View,
  Text,
  StyleSheet,
  Alert,
  ActivityIndicator,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import fileUploadService, { UploadResult } from '../services/fileUploadService';

interface DocumentUploadButtonProps {
  onDocumentUploaded: (uri: string, fileName: string) => void;
  buttonText?: string;
  iconName?: keyof typeof Ionicons.glyphMap;
  style?: any;
  disabled?: boolean;
  acceptedTypes?: string[];
}

export default function DocumentUploadButton({
  onDocumentUploaded,
  buttonText = 'Upload Document',
  iconName = 'document-outline',
  style,
  disabled = false,
  acceptedTypes = ['PDF', 'Images'],
}: DocumentUploadButtonProps) {
  const [uploading, setUploading] = React.useState(false);

  const handleUpload = async () => {
    if (disabled || uploading) return;

    setUploading(true);

    try {
      const result = await fileUploadService.pickDocument();
      
      if (result.success && result.uri && result.fileName) {
        try {
          const localUri = await fileUploadService.saveToLocalStorage(
            result.uri,
            result.fileName
          );
          onDocumentUploaded(localUri, result.fileName);
          Alert.alert('Success', `${result.fileName} uploaded successfully!`);
        } catch (error: any) {
          Alert.alert('Error', error.message);
        }
      } else if (result.error) {
        Alert.alert('Error', result.error);
      }
    } catch (error: any) {
      Alert.alert('Error', error.message);
    } finally {
      setUploading(false);
    }
  };

  return (
    <TouchableOpacity
      style={[
        styles.button,
        disabled && styles.disabled,
        style,
      ]}
      onPress={handleUpload}
      disabled={disabled || uploading}
    >
      <View style={styles.buttonContent}>
        {uploading ? (
          <ActivityIndicator color="#16a34a" />
        ) : (
          <>
            <Ionicons name={iconName} size={20} color="#16a34a" />
            <Text style={styles.buttonText}>{buttonText}</Text>
          </>
        )}
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  button: {
    backgroundColor: '#f0fdf4',
    borderWidth: 1,
    borderColor: '#16a34a',
    borderRadius: 8,
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  disabled: {
    opacity: 0.6,
  },
  buttonContent: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
  },
  buttonText: {
    color: '#16a34a',
    fontSize: 14,
    fontWeight: '600',
  },
});
