import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  Alert,
  Switch,
  Image,
  TextInput,
  Button,
  ActivityIndicator,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import DocumentUploadButton from '../core/components/DocumentUploadButton';
import ImageUploadButton from '../core/components/ImageUploadButton';
import MediaHelper from '../core/helpers/mediaHelper';
import FileUploadApi from '../core/api/fileUploadApi';

interface UploadedFile {
  fileName: string;
  uri: string;
  timestamp: number;
}

export default function UploadTestScreen() {
  const [uploadedImages, setUploadedImages] = useState<UploadedFile[]>([]);
  const [uploadedDocuments, setUploadedDocuments] = useState<UploadedFile[]>([]);
  const [testing, setTesting] = useState(false);
  const [testResults, setTestResults] = useState<string[]>([]);
  const [autoTest, setAutoTest] = useState(false);

  const addTestResult = (result: string) => {
    setTestResults(prev => [...prev, `${new Date().toLocaleTimeString()}: ${result}`]);
  };

  const handleImageUpload = async (uri: string) => {
    try {
      // In a real app, we would upload here. For the test screen, we just display it.
      // If we wanted to test the API:
      // await FileUploadApi.uploadProfilePicture('test-user-id', uri);

      const fileName = uri.split('/').pop() || 'image.jpg';
      const uploadedFile: UploadedFile = {
        fileName,
        uri,
        timestamp: Date.now(),
      };
      setUploadedImages(prev => [uploadedFile, ...prev]);
      addTestResult(`✅ Image selected: ${fileName}`);
    } catch (error: any) {
      addTestResult(`❌ Image selection failed: ${error.message}`);
    }
  };

  const handleDocumentUpload = async (uri: string, fileName: string) => {
    try {
      // await FileUploadApi.uploadDocument('test-user-id', uri, 'DOC');

      const uploadedFile: UploadedFile = {
        fileName,
        uri,
        timestamp: Date.now(),
      };
      setUploadedDocuments(prev => [uploadedFile, ...prev]);
      addTestResult(`✅ Document selected: ${fileName}`);
    } catch (error: any) {
      addTestResult(`❌ Document selection failed: ${error.message}`);
    }
  };

  const runPermissionTest = async () => {
    setTesting(true);
    addTestResult('🧪 Starting permission tests...');

    try {
      // Test camera permission
      addTestResult('📱 Testing camera permission...');
      const cameraResult = await MediaHelper.takePhoto();
      if (cameraResult.success || cameraResult.error?.includes('cancelled')) {
        addTestResult('✅ Camera permission granted (or cancelled)');
      } else {
        addTestResult(`⚠️ Camera permission: ${cameraResult.error}`);
      }

      // Test gallery permission
      addTestResult('🖼️ Testing gallery permission...');
      const galleryResult = await MediaHelper.pickImageFromLibrary();
      if (galleryResult.success || galleryResult.error?.includes('cancelled')) {
        addTestResult('✅ Gallery permission granted (or cancelled)');
      } else {
        addTestResult(`⚠️ Gallery permission: ${galleryResult.error}`);
      }

      // Test document picker
      addTestResult('📄 Testing document picker...');
      const documentResult = await MediaHelper.pickDocument();
      if (documentResult.success || documentResult.error?.includes('cancelled')) {
        addTestResult('✅ Document picker working (or cancelled)');
      } else {
        addTestResult(`⚠️ Document picker: ${documentResult.error}`);
      }

      addTestResult('✅ Permission tests completed');
    } catch (error: any) {
      addTestResult(`❌ Test failed: ${error.message}`);
    } finally {
      setTesting(false);
    }
  };

  const clearTestData = () => {
    setUploadedImages([]);
    setUploadedDocuments([]);
    setTestResults([]);
    addTestResult('🧹 Test data cleared');
  };

  const simulateUploadTest = async () => {
    setTesting(true);
    addTestResult('🔧 Running mock upload test...');

    try {
      // Mock image upload
      const mockImageUri = 'mock://test-image.jpg';
      await handleImageUpload(mockImageUri);

      // Mock document upload
      const mockDocUri = 'mock://test-document.pdf';
      await handleDocumentUpload(mockDocUri, 'test_document.pdf');

      addTestResult('✅ Mock upload test completed');
    } catch (error: any) {
      addTestResult(`❌ Mock test failed: ${error.message}`);
    } finally {
      setTesting(false);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Upload Functionality Test</Text>
        <Text style={styles.subtitle}>
          Test photo and document upload with permission handling
        </Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Test Controls</Text>

        <View style={styles.controlRow}>
          <Text style={styles.controlLabel}>Auto Test Mode</Text>
          <Switch
            value={autoTest}
            onValueChange={setAutoTest}
            trackColor={{ false: '#767577', true: '#16a34a' }}
            thumbColor={autoTest ? '#ffffff' : '#f4f3f4'}
          />
        </View>

        <View style={styles.buttonRow}>
          <View style={styles.buttonContainer}>
            <Button
              title="Run Permission Test"
              onPress={runPermissionTest}
              color="#16a34a"
            />
          </View>
          <View style={styles.buttonContainer}>
            <Button
              title="Mock Upload Test"
              onPress={simulateUploadTest}
              color="#3b82f6"
            />
          </View>
        </View>

        <View style={styles.buttonRow}>
          <View style={styles.buttonContainer}>
            <Button
              title="Clear All Data"
              onPress={clearTestData}
              color="#ef4444"
            />
          </View>
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Image Upload Test</Text>
        <ImageUploadButton
          onImageUploaded={handleImageUpload}
          buttonText="Test Image Upload"
          iconName="image-outline"
        />
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Document Upload Test</Text>
        <DocumentUploadButton
          onDocumentUploaded={handleDocumentUpload}
          buttonText="Test Document Upload"
          iconName="document-text-outline"
        />
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Test Results</Text>
        {testing && (
          <View style={styles.loadingContainer}>
            <ActivityIndicator size="small" color="#16a34a" />
            <Text style={styles.loadingText}>Running tests...</Text>
          </View>
        )}
        <View style={styles.resultsContainer}>
          {testResults.map((result, index) => (
            <Text key={index} style={styles.resultText}>
              {result}
            </Text>
          ))}
        </View>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Uploaded Images</Text>
        {uploadedImages.length === 0 ? (
          <Text style={styles.emptyText}>No images uploaded yet</Text>
        ) : (
          uploadedImages.map((image, index) => (
            <View key={index} style={styles.fileItem}>
              <Ionicons name="image" size={16} color="#16a34a" />
              <Text style={styles.fileName}>{image.fileName}</Text>
              <Text style={styles.fileTime}>
                {new Date(image.timestamp).toLocaleTimeString()}
              </Text>
            </View>
          ))
        )}
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Uploaded Documents</Text>
        {uploadedDocuments.length === 0 ? (
          <Text style={styles.emptyText}>No documents uploaded yet</Text>
        ) : (
          uploadedDocuments.map((doc, index) => (
            <View key={index} style={styles.fileItem}>
              <Ionicons name="document" size={16} color="#3b82f6" />
              <Text style={styles.fileName}>{doc.fileName}</Text>
              <Text style={styles.fileTime}>
                {new Date(doc.timestamp).toLocaleTimeString()}
              </Text>
            </View>
          ))
        )}
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Testing Instructions</Text>
        <View style={styles.instructionContainer}>
          <Text style={styles.instruction}>
            1. Tap "Run Permission Test" to test camera and gallery permissions
          </Text>
          <Text style={styles.instruction}>
            2. Try uploading real images using the upload buttons
          </Text>
          <Text style={styles.instruction}>
            3. Try uploading PDF or document files
          </Text>
          <Text style={styles.instruction}>
            4. Check that files are saved to local storage
          </Text>
          <Text style={styles.instruction}>
            5. Test with both granted and denied permissions
          </Text>
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8fafc',
  },
  header: {
    backgroundColor: '#ffffff',
    padding: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#e2e8f0',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#1e293b',
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 14,
    color: '#64748b',
  },
  section: {
    backgroundColor: '#ffffff',
    margin: 16,
    padding: 16,
    borderRadius: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.1,
    shadowRadius: 2,
    elevation: 2,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#1e293b',
    marginBottom: 12,
  },
  controlRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  controlLabel: {
    fontSize: 16,
    color: '#374151',
  },
  buttonRow: {
    flexDirection: 'row',
    marginBottom: 12,
  },
  buttonContainer: {
    flex: 1,
    marginHorizontal: 4,
  },
  loadingContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  loadingText: {
    marginLeft: 8,
    fontSize: 14,
    color: '#6b7280',
  },
  resultsContainer: {
    backgroundColor: '#f8fafc',
    padding: 12,
    borderRadius: 6,
    maxHeight: 200,
  },
  resultText: {
    fontSize: 12,
    color: '#374151',
    marginBottom: 2,
    fontFamily: 'monospace',
  },
  fileItem: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 8,
    backgroundColor: '#f8fafc',
    borderRadius: 6,
    marginBottom: 4,
  },
  fileName: {
    flex: 1,
    marginLeft: 8,
    fontSize: 14,
    color: '#374151',
  },
  fileTime: {
    fontSize: 12,
    color: '#6b7280',
  },
  emptyText: {
    fontSize: 14,
    color: '#9ca3af',
    fontStyle: 'italic',
    textAlign: 'center',
    padding: 20,
  },
  instructionContainer: {
    backgroundColor: '#eff6ff',
    padding: 12,
    borderRadius: 6,
    borderLeftWidth: 4,
    borderLeftColor: '#3b82f6',
  },
  instruction: {
    fontSize: 14,
    color: '#1e40af',
    marginBottom: 4,
  },
});