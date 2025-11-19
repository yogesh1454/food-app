import { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Alert,
  Modal,
  FlatList,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { useDispatch } from 'react-redux';
import { RootStackParamList } from '../navigation/AppNavigator';
import { setRestaurant } from '../store/slices/restaurantSlice';
import { setFirstTime } from '../store/slices/authSlice';
import { commonStyles } from '../core/styles/commonStyles';
import { colors } from '../core/constants/colors';
import ImageUploadButton from '../core/components/ImageUploadButton';
import DocumentUploadButton from '../core/components/DocumentUploadButton';

type OnboardingScreenNavigationProp = StackNavigationProp<RootStackParamList, 'Onboarding'>;

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  gradient: {
    flex: 1,
  },
  scrollView: {
    flex: 1,
  },
  content: {
    ...commonStyles.p6,
    ...commonStyles.px6,
  },
  header: {
    ...commonStyles.row,
    ...commonStyles.justifyBetween,
    ...commonStyles.itemsCenter,
    ...commonStyles.my4,
  },
  skipText: {
    color: colors.text,
    ...commonStyles.textBase,
    ...commonStyles.fontMedium,
  },
  indicators: {
    ...commonStyles.row,
    gap: 12,
    ...commonStyles.itemsCenter,
  },
  indicator: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: 'rgba(255, 255, 255, 0.4)',
  },
  indicatorActive: {
    backgroundColor: 'white',
    width: 20,
  },
  pageContent: {
    flex: 1,
  },
  title: {
    ...commonStyles.text3xl,
    ...commonStyles.fontBold,
    color: colors.text,
    ...commonStyles.textCenter,
    ...commonStyles.my4,
    lineHeight: 36,
  },
  subtitle: {
    ...commonStyles.textBase,
    color: colors.textSecondary,
    ...commonStyles.textCenter,
    ...commonStyles.my4,
    lineHeight: 24,
  },
  section: {
    ...commonStyles.my4,
  },
  sectionTitle: {
    ...commonStyles.textLg,
    ...commonStyles.fontSemibold,
    color: colors.text,
    ...commonStyles.my2,
  },
  input: {
    ...commonStyles.bgWhite,
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.my2,
    ...commonStyles.textBase,
    borderWidth: 1,
    borderColor: colors.border,
    ...commonStyles.shadow,
    color: colors.text,
  },
  inputFocused: {
    borderColor: colors.primary,
    shadowOpacity: 0.2,
  },
  textArea: {
    height: 120,
    textAlignVertical: 'top',
  },
  inputField: {
    flex: 1,
    ...commonStyles.bgWhite,
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.textBase,
    borderWidth: 1,
    borderColor: colors.border,
    ...commonStyles.shadow,
    color: colors.text,
  },
  dropdown: {
    ...commonStyles.bgWhite,
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.my2,
    borderWidth: 1,
    borderColor: colors.border,
    ...commonStyles.row,
    ...commonStyles.justifyBetween,
    ...commonStyles.itemsCenter,
    ...commonStyles.shadow,
  },
  dropdownText: {
    ...commonStyles.textBase,
    color: colors.text,
    ...commonStyles.fontMedium,
  },
  dropdownPlaceholder: {
    ...commonStyles.textBase,
    color: colors.textMuted,
  },
  timeButton: {
    ...commonStyles.bgWhite,
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.my2,
    borderWidth: 1,
    borderColor: colors.border,
    ...commonStyles.row,
    ...commonStyles.justifyBetween,
    ...commonStyles.itemsCenter,
    ...commonStyles.shadow,
  },
  timeText: {
    ...commonStyles.textBase,
    color: colors.text,
    ...commonStyles.fontMedium,
  },
  timePlaceholder: {
    ...commonStyles.textBase,
    color: colors.textMuted,
  },
  uploadButton: {
    backgroundColor: 'rgba(255, 255, 255, 0.15)',
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.itemsCenter,
    ...commonStyles.my2,
    borderWidth: 2,
    borderColor: 'rgba(255, 255, 255, 0.3)',
    borderStyle: 'dashed',
  },
  uploadText: {
    color: colors.text,
    ...commonStyles.textBase,
    ...commonStyles.fontMedium,
  },
  extractedItems: {
    ...commonStyles.bgWhite,
    ...commonStyles.roundedLg,
    ...commonStyles.p4,
    ...commonStyles.my3,
    ...commonStyles.shadowLg,
  },
  item: {
    ...commonStyles.py3,
    borderBottomWidth: 1,
    borderBottomColor: colors.borderLight,
  },
  itemText: {
    ...commonStyles.textBase,
    color: colors.text,
    ...commonStyles.fontMedium,
  },
  navigation: {
    ...commonStyles.row,
    ...commonStyles.justifyBetween,
    ...commonStyles.my4,
    gap: 16,
  },
  button: {
    ...commonStyles.bgWhite,
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.itemsCenter,
    flex: 1,
    ...commonStyles.shadowLg,
  },
  buttonText: {
    color: 'white',
    ...commonStyles.textBase,
    ...commonStyles.fontSemibold,
  },
  buttonDisabled: {
    backgroundColor: colors.borderLight,
    shadowOpacity: 0.05,
  },
  buttonTextDisabled: {
    color: colors.textMuted,
  },
  otpButton: {
    backgroundColor: colors.primary,
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.itemsCenter,
    ...commonStyles.shadowLg,
  },
  navigationButton: {
    backgroundColor: colors.primary,
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.itemsCenter,
    flex: 1,
    ...commonStyles.shadowLg,
  },
  navigationButtonDisabled: {
    backgroundColor: colors.borderLight,
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.itemsCenter,
    flex: 1,
    ...commonStyles.shadowLg,
    shadowOpacity: 0.05,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
    ...commonStyles.justifyCenter,
    ...commonStyles.itemsCenter,
  },
  modalContent: {
    ...commonStyles.bgWhite,
    ...commonStyles.roundedXl,
    ...commonStyles.p6,
    width: '85%',
    maxHeight: '70%',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.25,
    shadowRadius: 20,
    elevation: 10,
  },
  modalTitle: {
    ...commonStyles.text2xl,
    ...commonStyles.fontBold,
    color: colors.text,
    ...commonStyles.my4,
    ...commonStyles.textCenter,
  },
  otpInput: {
    backgroundColor: colors.background,
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.textLg,
    ...commonStyles.textCenter,
    ...commonStyles.my4,
    borderWidth: 1,
    borderColor: colors.border,
    ...commonStyles.fontSemibold,
    color: colors.text,
  },
  menuOption: {
    ...commonStyles.row,
    ...commonStyles.justifyBetween,
    ...commonStyles.my3,
    gap: 12,
  },
  menuOptionButton: {
    ...commonStyles.px4,
    ...commonStyles.py3,
    ...commonStyles.roundedLg,
    borderWidth: 2,
    borderColor: colors.border,
    ...commonStyles.bgWhite,
    flex: 1,
    ...commonStyles.itemsCenter,
  },
  menuOptionButtonActive: {
    borderColor: colors.primary,
    ...commonStyles.bgGreen50,
  },
  menuOptionText: {
    color: colors.textSecondary,
    ...commonStyles.textSm,
    ...commonStyles.fontMedium,
  },
  menuOptionTextActive: {
    color: colors.primary,
    ...commonStyles.fontSemibold,
  },
  inputContainer: {
    ...commonStyles.row,
    ...commonStyles.itemsCenter,
    ...commonStyles.my4,
    gap: 12,
  },
  verifyButton: {
    backgroundColor: colors.primary,
    ...commonStyles.px4,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.shadow,
  },
  verifiedButton: {
    backgroundColor: colors.success,
  },
  verifyText: {
    ...commonStyles.textWhite,
    ...commonStyles.textSm,
    ...commonStyles.fontSemibold,
  },
  verifiedText: {
    ...commonStyles.textWhite,
  },
  headerTitle: {
    ...commonStyles.text2xl,
    ...commonStyles.fontBold,
    color: colors.text,
    ...commonStyles.textCenter,
  },
  dropdownItem: {
    ...commonStyles.px6,
    ...commonStyles.py4,
    borderBottomWidth: 1,
    borderBottomColor: colors.borderLight,
  },
  dropdownItemText: {
    ...commonStyles.textLg,
    color: colors.text,
    ...commonStyles.fontMedium,
  },
  timeItem: {
    ...commonStyles.px6,
    ...commonStyles.py4,
    borderBottomWidth: 1,
    borderBottomColor: colors.borderLight,
  },
  timeItemText: {
    ...commonStyles.textLg,
    color: colors.text,
    ...commonStyles.fontMedium,
  },
  progressContainer: {
    ...commonStyles.itemsCenter,
    ...commonStyles.my4,
  },
  progressText: {
    color: colors.textSecondary,
    ...commonStyles.textSm,
    ...commonStyles.my1,
  },
  modalHeader: {
    ...commonStyles.row,
    ...commonStyles.justifyBetween,
    ...commonStyles.itemsCenter,
    ...commonStyles.px6,
    ...commonStyles.py4,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    ...commonStyles.bgWhite,
  },
  modalHeaderTitle: {
    ...commonStyles.textLg,
    ...commonStyles.fontSemibold,
    color: colors.text,
  },
});

interface OnboardingData {
  name: string;
  cuisineType: string;
  address: string;
  openingTime: string;
  closingTime: string;
  phone: string;
  email: string;
  gstNumber: string;
  fssaiNumber: string;
  licenseDocuments: string[];
  logoUrl: string;
  coverPhotoUrl: string;
  menuText: string;
  extractedItems: string[];
  phoneVerified: boolean;
  emailVerified: boolean;
}

const cuisines = [
  'Italian', 'Chinese', 'Indian', 'Mexican', 'Japanese', 'Thai', 'French', 'Mediterranean', 'American', 'Other'
];

export default function OnboardingScreen() {
  const [currentPage, setCurrentPage] = useState(0);
  const [cuisineDropdownVisible, setCuisineDropdownVisible] = useState(false);
  const [timePickerVisible, setTimePickerVisible] = useState(false);
  const [timeType, setTimeType] = useState<'opening' | 'closing'>('opening');
  const [otpModalVisible, setOtpModalVisible] = useState(false);
  const [otpType, setOtpType] = useState<'phone' | 'email'>('phone');
  const [otp, setOtp] = useState('');
  const [menuOption, setMenuOption] = useState<'photo' | 'pdf' | 'manual'>('manual');
  const [data, setData] = useState<OnboardingData>({
    name: '',
    cuisineType: '',
    address: '',
    openingTime: '',
    closingTime: '',
    phone: '',
    email: '',
    gstNumber: '',
    fssaiNumber: '',
    licenseDocuments: [],
    logoUrl: '',
    coverPhotoUrl: '',
    menuText: '',
    extractedItems: [],
    phoneVerified: false,
    emailVerified: false,
  });
  const navigation = useNavigation<OnboardingScreenNavigationProp>();
  const dispatch = useDispatch();

  const pages = [
    { title: 'Restaurant Details' },
    { title: 'Contact Information' },
    { title: 'License & Documents' },
    { title: 'Photos' },
    { title: 'Menu Upload' },
  ];

  const handleNext = () => {
    if (currentPage < pages.length - 1) {
      setCurrentPage(currentPage + 1);
    } else {
      handleComplete();
    }
  };

  const handlePrevious = () => {
    if (currentPage > 0) {
      setCurrentPage(currentPage - 1);
    }
  };

  const handleComplete = () => {
    // Mock completion: set restaurant data and mark as not first time
    dispatch(setRestaurant({
      id: '1',
      name: data.name,
      cuisineType: data.cuisineType,
      description: '',
      address: data.address,
      phone: data.phone,
      email: data.email,
      logoUrl: data.logoUrl,
      coverPhotoUrl: data.coverPhotoUrl,
      isOpen: true,
      operatingHours: {
        Monday: { open: data.openingTime, close: data.closingTime, isOpen: true },
      },
      gstNumber: data.gstNumber,
      fssaiNumber: data.fssaiNumber,
      licenseDocuments: data.licenseDocuments,
      menuItems: data.extractedItems,
      staff: [],
    }));
    dispatch(setFirstTime(false));
    navigation.navigate('PostOnboarding');
  };

  const handleSkip = () => {
    navigation.navigate('Main');
  };

  const extractMenuItems = () => {
    // Mock extraction: split by lines and filter non-empty
    const items = data.menuText.split('\n').filter(item => item.trim() !== '');
    setData({ ...data, extractedItems: items });
  };

  const handleVerifyPhone = () => {
    setOtpType('phone');
    setOtpModalVisible(true);
  };

  const handleVerifyEmail = () => {
    setOtpType('email');
    setOtpModalVisible(true);
  };

  const handleOtpSubmit = () => {
    if (otp === '1234') { // Mock OTP
      if (otpType === 'phone') {
        setData({ ...data, phoneVerified: true });
      } else {
        setData({ ...data, emailVerified: true });
      }
      setOtpModalVisible(false);
      setOtp('');
      Alert.alert('Success', `${otpType === 'phone' ? 'Phone' : 'Email'} verified successfully!`);
    } else {
      Alert.alert('Error', 'Invalid OTP');
    }
  };

  const handleTimeSelect = (time: string) => {
    if (timeType === 'opening') {
      setData({ ...data, openingTime: time });
    } else {
      setData({ ...data, closingTime: time });
    }
    setTimePickerVisible(false);
  };

  const renderPage = () => {
    switch (currentPage) {
      case 0:
        return (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Basic Information</Text>
            <TextInput
              style={styles.input}
              placeholder="Enter your restaurant's full name"
              placeholderTextColor="#6b7280"
              value={data.name}
              onChangeText={(text) => setData({ ...data, name: text })}
              accessibilityLabel="Restaurant Name Input"
            />
            <TouchableOpacity
              style={styles.dropdown}
              onPress={() => setCuisineDropdownVisible(true)}
              accessibilityLabel="Select Cuisine Type"
            >
              <Text style={data.cuisineType ? styles.dropdownText : styles.dropdownPlaceholder}>
                {data.cuisineType || 'Select Cuisine Type'}
              </Text>
              <Ionicons name="chevron-down" size={20} color="#6b7280" />
            </TouchableOpacity>
            <TextInput
              style={styles.input}
              placeholder="Enter complete restaurant address with landmark"
              placeholderTextColor="#6b7280"
              value={data.address}
              onChangeText={(text) => setData({ ...data, address: text })}
              accessibilityLabel="Restaurant Address Input"
            />
            <Text style={styles.sectionTitle}>Operating Hours</Text>
            <TouchableOpacity
              style={styles.timeButton}
              onPress={() => {
                setTimeType('opening');
                setTimePickerVisible(true);
              }}
              accessibilityLabel="Select Opening Time"
            >
              <Text style={data.openingTime ? styles.timeText : styles.timePlaceholder}>
                {data.openingTime || 'Select Opening Time'}
              </Text>
              <Ionicons name="time" size={20} color="#6b7280" />
            </TouchableOpacity>
            <TouchableOpacity
              style={styles.timeButton}
              onPress={() => {
                setTimeType('closing');
                setTimePickerVisible(true);
              }}
              accessibilityLabel="Select Closing Time"
            >
              <Text style={data.closingTime ? styles.timeText : styles.timePlaceholder}>
                {data.closingTime || 'Select Closing Time'}
              </Text>
              <Ionicons name="time" size={20} color="#6b7280" />
            </TouchableOpacity>
          </View>
        );
      case 1:
        return (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Contact Information</Text>
            <Text style={styles.subtitle}>We'll verify these details for security</Text>
            <View style={styles.inputContainer}>
              <TextInput
                style={styles.inputField}
                placeholder="Phone Number"
                placeholderTextColor="#6b7280"
                value={data.phone}
                onChangeText={(text) => setData({ ...data, phone: text })}
                keyboardType="phone-pad"
                accessibilityLabel="Phone Number Input"
              />
              <TouchableOpacity
                style={[styles.verifyButton, data.phoneVerified && styles.verifiedButton]}
                onPress={handleVerifyPhone}
                disabled={data.phoneVerified}
                accessibilityLabel="Verify Phone Number"
              >
                <Text style={[styles.verifyText, data.phoneVerified && styles.verifiedText]}>
                  {data.phoneVerified ? '✓' : 'Verify'}
                </Text>
              </TouchableOpacity>
            </View>
            <View style={styles.inputContainer}>
              <TextInput
                style={styles.inputField}
                placeholder="Email Address"
                placeholderTextColor="#6b7280"
                value={data.email}
                onChangeText={(text) => setData({ ...data, email: text })}
                keyboardType="email-address"
                accessibilityLabel="Email Address Input"
              />
              <TouchableOpacity
                style={[styles.verifyButton, data.emailVerified && styles.verifiedButton]}
                onPress={handleVerifyEmail}
                disabled={data.emailVerified}
                accessibilityLabel="Verify Email Address"
              >
                <Text style={[styles.verifyText, data.emailVerified && styles.verifiedText]}>
                  {data.emailVerified ? '✓' : 'Verify'}
                </Text>
              </TouchableOpacity>
            </View>
          </View>
        );
      case 2:
        return (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>License Information</Text>
            <Text style={styles.subtitle}>Enter your business registration details</Text>
            <TextInput
              style={styles.input}
              placeholder="GST Number"
              placeholderTextColor="#6b7280"
              value={data.gstNumber}
              onChangeText={(text) => setData({ ...data, gstNumber: text })}
              accessibilityLabel="GST Number Input"
            />
            <TextInput
              style={styles.input}
              placeholder="FSSAI License Number"
              placeholderTextColor="#6b7280"
              value={data.fssaiNumber}
              onChangeText={(text) => setData({ ...data, fssaiNumber: text })}
              accessibilityLabel="FSSAI License Number Input"
            />
            <Text style={styles.sectionTitle}>Supporting Documents</Text>
            <DocumentUploadButton
              onDocumentUploaded={(uri, fileName) => {
                setData({ ...data, licenseDocuments: [...data.licenseDocuments, uri] });
              }}
              buttonText="Upload GST Document"
              style={{ marginBottom: 16 }}
            />
            <DocumentUploadButton
              onDocumentUploaded={(uri, fileName) => {
                setData({ ...data, licenseDocuments: [...data.licenseDocuments, uri] });
              }}
              buttonText="Upload FSSAI Document"
              style={{ marginBottom: 16 }}
            />
          </View>
        );
      case 3:
        return (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Restaurant Photos</Text>
            <Text style={styles.subtitle}>Add visual elements to make your restaurant stand out</Text>
            <ImageUploadButton
              onImageUploaded={(uri) => {
                setData({ ...data, logoUrl: uri });
              }}
              buttonText="Upload Restaurant Logo (Optional)"
              iconName="image-outline"
              style={{ marginBottom: 16 }}
            />
            <ImageUploadButton
              onImageUploaded={(uri) => {
                setData({ ...data, coverPhotoUrl: uri });
              }}
              buttonText="Upload Cover Photo (Optional)"
              iconName="images-outline"
              style={{ marginBottom: 16 }}
            />
          </View>
        );
      case 4:
        return (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Menu Setup</Text>
            <Text style={styles.subtitle}>Choose how you'd like to add your menu</Text>
            <View style={styles.menuOption}>
              <TouchableOpacity
                style={[styles.menuOptionButton, menuOption === 'photo' && styles.menuOptionButtonActive]}
                onPress={() => setMenuOption('photo')}
                accessibilityLabel="Select Menu from Photo"
              >
                <Text style={[styles.menuOptionText, menuOption === 'photo' && styles.menuOptionTextActive]}>
                  From Photo
                </Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.menuOptionButton, menuOption === 'pdf' && styles.menuOptionButtonActive]}
                onPress={() => setMenuOption('pdf')}
                accessibilityLabel="Select Menu from PDF"
              >
                <Text style={[styles.menuOptionText, menuOption === 'pdf' && styles.menuOptionTextActive]}>
                  From PDF
                </Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.menuOptionButton, menuOption === 'manual' && styles.menuOptionButtonActive]}
                onPress={() => setMenuOption('manual')}
                accessibilityLabel="Select Manual Menu Entry"
              >
                <Text style={[styles.menuOptionText, menuOption === 'manual' && styles.menuOptionTextActive]}>
                  Manual
                </Text>
              </TouchableOpacity>
            </View>
            {menuOption === 'manual' && (
              <TextInput
                style={[styles.input, styles.textArea]}
                placeholder="Paste or type your menu here..."
                placeholderTextColor="#6b7280"
                value={data.menuText}
                onChangeText={(text) => setData({ ...data, menuText: text })}
                multiline
                accessibilityLabel="Manual Menu Text Input"
              />
            )}
            {menuOption !== 'manual' && (
              menuOption === 'photo' ? (
                <ImageUploadButton
                  onImageUploaded={(uri) => {
                    setData({ ...data, menuText: `Menu photo uploaded: ${uri}` });
                  }}
                  buttonText="Upload Menu Photo"
                  iconName="camera-outline"
                  style={{ marginBottom: 16 }}
                />
              ) : (
                <DocumentUploadButton
                  onDocumentUploaded={(uri, fileName) => {
                    setData({ ...data, menuText: `Menu PDF uploaded: ${fileName}` });
                  }}
                  buttonText="Upload Menu PDF"
                  iconName="document-outline"
                  style={{ marginBottom: 16 }}
                />
              )
            )}
            <TouchableOpacity style={styles.button} onPress={extractMenuItems} accessibilityLabel="Extract Menu Items">
              <Text style={styles.buttonText}>Extract Menu Items</Text>
            </TouchableOpacity>
            {data.extractedItems.length > 0 && (
              <View style={styles.extractedItems}>
                <Text style={styles.sectionTitle}>Extracted Items:</Text>
                {data.extractedItems.map((item, index) => (
                  <View key={index} style={styles.item}>
                    <Text style={styles.itemText}>{item}</Text>
                  </View>
                ))}
              </View>
            )}
          </View>
        );
      default:
        return null;
    }
  };

  const isFormValid = () => {
    switch (currentPage) {
      case 0:
        return data.name && data.cuisineType && data.address && data.openingTime && data.closingTime;
      case 1:
        return data.phone && data.email && data.phoneVerified && data.emailVerified;
      case 2:
        return data.gstNumber && data.fssaiNumber;
      case 3:
        return true; // Optional
      case 4:
        return menuOption === 'manual' ? data.menuText : true;
      default:
        return false;
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <LinearGradient colors={['#ffffff', '#ffffff']} style={styles.gradient}>
        <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
          <View style={styles.content}>
            <View style={styles.header}>
              <TouchableOpacity onPress={handleSkip} accessibilityLabel="Skip Onboarding">
                <Text style={styles.skipText}>Skip</Text>
              </TouchableOpacity>
              <View style={styles.indicators}>
                {pages.map((_, index) => (
                  <View
                    key={index}
                    style={[
                      styles.indicator,
                      index === currentPage && styles.indicatorActive,
                    ]}
                  />
                ))}
              </View>
            </View>
            <View style={styles.progressContainer}>
              <Text style={styles.title}>{pages[currentPage].title}</Text>
              <Text style={styles.progressText}>
                Step {currentPage + 1} of {pages.length}
              </Text>
            </View>
            <View style={styles.pageContent}>{renderPage()}</View>
            <View style={styles.navigation}>
              <TouchableOpacity
                style={[styles.navigationButton, currentPage === 0 && styles.navigationButtonDisabled]}
                onPress={handlePrevious}
                disabled={currentPage === 0}
                accessibilityLabel="Previous Step"
              >
                <Text style={[styles.buttonText, currentPage === 0 && styles.buttonTextDisabled]}>
                  Previous
                </Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.navigationButton, !isFormValid() && styles.navigationButtonDisabled]}
                onPress={handleNext}
                disabled={!isFormValid()}
                accessibilityLabel={currentPage === pages.length - 1 ? "Complete Onboarding" : "Next Step"}
              >
                <Text style={[styles.buttonText, !isFormValid() && styles.buttonTextDisabled]}>
                  {currentPage === pages.length - 1 ? 'Complete' : 'Next'}
                </Text>
              </TouchableOpacity>
            </View>
          </View>
        </ScrollView>
      </LinearGradient>

      {/* Cuisine Dropdown Modal */}
      <Modal visible={cuisineDropdownVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => setCuisineDropdownVisible(false)} accessibilityLabel="Close Cuisine Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Select Cuisine Type</Text>
              <View />
            </View>
            <FlatList
              data={cuisines}
              keyExtractor={(item) => item}
              renderItem={({ item }) => (
                <TouchableOpacity
                  style={styles.dropdownItem}
                  onPress={() => {
                    setData({ ...data, cuisineType: item });
                    setCuisineDropdownVisible(false);
                  }}
                  accessibilityLabel={`Select ${item} Cuisine`}
                >
                  <Text style={styles.dropdownItemText}>{item}</Text>
                </TouchableOpacity>
              )}
            />
          </View>
        </View>
      </Modal>

      {/* Time Picker Modal */}
      <Modal visible={timePickerVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => setTimePickerVisible(false)} accessibilityLabel="Close Time Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Select Time</Text>
              <View />
            </View>
            <Text style={styles.sectionTitle}>Select {timeType === 'opening' ? 'Opening' : 'Closing'} Time</Text>
            <FlatList
              data={Array.from({ length: 24 }, (_, i) => `${i.toString().padStart(2, '0')}:00`)}
              keyExtractor={(item) => item}
              renderItem={({ item }) => (
                <TouchableOpacity
                  style={styles.timeItem}
                  onPress={() => handleTimeSelect(item)}
                  accessibilityLabel={`Select Time ${item}`}
                >
                  <Text style={styles.timeItemText}>{item}</Text>
                </TouchableOpacity>
              )}
            />
          </View>
        </View>
      </Modal>

      {/* OTP Modal */}
      <Modal visible={otpModalVisible} animationType="slide" transparent={true}>
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <View style={styles.modalHeader}>
              <TouchableOpacity onPress={() => setOtpModalVisible(false)} accessibilityLabel="Close OTP Modal">
                <Ionicons name="close" size={24} color="#111827" />
              </TouchableOpacity>
              <Text style={styles.modalTitle}>Verify {otpType === 'phone' ? 'Phone' : 'Email'}</Text>
              <View />
            </View>
            <Text style={styles.sectionTitle}>Enter OTP</Text>
            <Text style={styles.subtitle}>Mock OTP: 1234</Text>
            <TextInput
              style={styles.otpInput}
              placeholder="Enter OTP"
              placeholderTextColor="#6b7280"
              value={otp}
              onChangeText={setOtp}
              keyboardType="numeric"
              accessibilityLabel="OTP Input"
            />
            <TouchableOpacity style={styles.otpButton} onPress={handleOtpSubmit} accessibilityLabel="Submit OTP">
              <Text style={styles.buttonText}>Verify</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}