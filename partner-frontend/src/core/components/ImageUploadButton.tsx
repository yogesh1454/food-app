import React, { useState } from 'react';
import {
  TouchableOpacity,
  View,
  Text,
  StyleSheet,
  Alert,
  ActivityIndicator,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { FileUploadService } from '../services/fileUploadService';
import ImagePickerModal from './ImagePickerModal';

interface ImageUploadButtonProps {
  onImageUploaded: (uri: string) => void;
  buttonText?: string;
  iconName?: keyof typeof Ionicons.glyphMap;
  style?: any;
  disabled?: boolean;
}

export default function ImageUploadButton({
  onImageUploaded,
  buttonText = 'Upload Image',
  iconName = 'camera-outline',
  style,
  disabled = false,
}: ImageUploadButtonProps) {
  const [uploading, setUploading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);

  const handleUpload = async () => {
    if (disabled || uploading) return;
    setModalVisible(true);
  };

  const handleImageSelected = async (uri: string) => {
    setUploading(true);
    
    try {
      onImageUploaded(uri);
    } catch (error: any) {
      Alert.alert('Error', error.message);
    } finally {
      setUploading(false);
      setModalVisible(false);
    }
  };

  const handleCloseModal = () => {
    setModalVisible(false);
  };

  return (
    <>
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
      
      <ImagePickerModal
        visible={modalVisible}
        onClose={handleCloseModal}
        onImageSelected={handleImageSelected}
      />
    </>
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
