import { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  ActivityIndicator,
  Alert,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import axios from 'axios';
import { RootStackParamList } from '../navigation/AppNavigator';
import { commonStyles } from '../core/styles/commonStyles';
import { colors } from '../core/constants/colors';

type PostOnboardingScreenNavigationProp = StackNavigationProp<RootStackParamList, 'PostOnboarding'>;

interface Post {
  userId: number;
  id: number;
  title: string;
  body: string;
}

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
  loadingContainer: {
    ...commonStyles.flex1,
    ...commonStyles.justifyCenter,
    ...commonStyles.itemsCenter,
  },
  loadingText: {
    ...commonStyles.textBase,
    color: colors.textSecondary,
    ...commonStyles.mt4,
  },
  dataContainer: {
    ...commonStyles.bgWhite,
    ...commonStyles.roundedLg,
    ...commonStyles.p4,
    ...commonStyles.my4,
    ...commonStyles.shadowLg,
  },
  dataTitle: {
    ...commonStyles.textLg,
    ...commonStyles.fontSemibold,
    color: colors.text,
    ...commonStyles.mb3,
  },
  dataItem: {
    ...commonStyles.p4,
    ...commonStyles.mb3,
    ...commonStyles.roundedLg,
    backgroundColor: colors.background,
    borderLeftWidth: 4,
    borderLeftColor: colors.primary,
  },
  dataItemTitle: {
    ...commonStyles.textBase,
    ...commonStyles.fontSemibold,
    color: colors.text,
    ...commonStyles.mb2,
  },
  dataItemBody: {
    ...commonStyles.textSm,
    color: colors.textSecondary,
    lineHeight: 20,
  },
  errorContainer: {
    ...commonStyles.bgError,
    ...commonStyles.p4,
    ...commonStyles.roundedLg,
    ...commonStyles.my4,
  },
  errorText: {
    ...commonStyles.textBase,
    color: colors.error,
    ...commonStyles.textCenter,
  },
  actionButton: {
    backgroundColor: colors.primary,
    ...commonStyles.px6,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.itemsCenter,
    ...commonStyles.my6,
    ...commonStyles.shadowLg,
  },
  actionButtonText: {
    ...commonStyles.textWhite,
    ...commonStyles.textBase,
    ...commonStyles.fontSemibold,
  },
  buttonContainer: {
    ...commonStyles.row,
    ...commonStyles.justifyBetween,
    ...commonStyles.my4,
    gap: 16,
  },
  secondaryButton: {
    backgroundColor: 'transparent',
    borderWidth: 2,
    borderColor: colors.primary,
    ...commonStyles.px6,
    ...commonStyles.py4,
    ...commonStyles.roundedLg,
    ...commonStyles.itemsCenter,
    flex: 1,
  },
  secondaryButtonText: {
    color: colors.primary,
    ...commonStyles.textBase,
    ...commonStyles.fontSemibold,
  },
  headerTitle: {
    ...commonStyles.text2xl,
    ...commonStyles.fontBold,
    color: colors.text,
    ...commonStyles.textCenter,
  },
});

export default function PostOnboardingScreen() {
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigation = useNavigation<PostOnboardingScreenNavigationProp>();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // Mock API call using JSONPlaceholder
      const response = await axios.get('https://jsonplaceholder.typicode.com/posts');
      setPosts(response.data.slice(0, 5)); // Get first 5 posts
    } catch (err) {
      console.error('Error fetching data:', err);
      setError('Failed to fetch data. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleRetry = () => {
    fetchData();
  };

  const handleContinue = () => {
    navigation.navigate('Main');
  };

  const handleGoBack = () => {
    navigation.goBack();
  };

  const renderContent = () => {
    if (loading) {
      return (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color={colors.primary} />
          <Text style={styles.loadingText}>Fetching data...</Text>
        </View>
      );
    }

    if (error) {
      return (
        <View style={styles.errorContainer}>
          <Text style={styles.errorText}>{error}</Text>
          <TouchableOpacity 
            style={[styles.actionButton, { marginTop: 16 }]} 
            onPress={handleRetry}
          >
            <Text style={styles.actionButtonText}>Retry</Text>
          </TouchableOpacity>
        </View>
      );
    }

    return (
      <View style={styles.dataContainer}>
        <Text style={styles.dataTitle}>Fetched Data (Mock API)</Text>
        {posts.map((post: Post) => (
          <View key={post.id} style={styles.dataItem}>
            <Text style={styles.dataItemTitle}>
              {post.title.charAt(0).toUpperCase() + post.title.slice(1)}
            </Text>
            <Text style={styles.dataItemBody}>{post.body}</Text>
          </View>
        ))}
      </View>
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      <LinearGradient colors={['#ffffff', '#ffffff']} style={styles.gradient}>
        <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
          <View style={styles.content}>
            <View style={styles.header}>
              <TouchableOpacity onPress={handleGoBack} accessibilityLabel="Go Back">
                <Ionicons name="arrow-back" size={24} color={colors.text} />
              </TouchableOpacity>
              <Text style={styles.headerTitle}>Data Loading</Text>
              <View style={{ width: 24 }} />
            </View>
            
            <Text style={styles.title}>Welcome to Your Dashboard!</Text>
            <Text style={styles.subtitle}>
              Your onboarding is complete. We're fetching some sample data to show you the app's capabilities.
            </Text>

            {renderContent()}

            {!loading && !error && (
              <View style={styles.buttonContainer}>
                <TouchableOpacity
                  style={styles.secondaryButton}
                  onPress={handleRetry}
                >
                  <Text style={styles.secondaryButtonText}>Refresh Data</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  style={styles.actionButton}
                  onPress={handleContinue}
                >
                  <Text style={styles.actionButtonText}>Continue to App</Text>
                </TouchableOpacity>
              </View>
            )}

            {!loading && !error && (
              <TouchableOpacity
                style={styles.actionButton}
                onPress={handleContinue}
              >
                <Text style={styles.actionButtonText}>Next</Text>
              </TouchableOpacity>
            )}
          </View>
        </ScrollView>
      </LinearGradient>
    </SafeAreaView>
  );
}