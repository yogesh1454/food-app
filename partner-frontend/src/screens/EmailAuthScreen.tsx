import React, { useState } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    SafeAreaView,
    StyleSheet,
    Alert,
    ActivityIndicator,
    KeyboardAvoidingView,
    Platform,
    ScrollView,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation, useRoute, RouteProp } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { createUserWithEmailAndPassword, signInWithEmailAndPassword } from 'firebase/auth';
import { auth } from '../core/config/firebase';
import { RootStackParamList } from '../navigation/AppNavigator';
import { commonStyles } from '../core/styles/commonStyles';
import { colors } from '../core/constants/colors';
import { useAppDispatch } from '../store';
import { hydrateRestaurant } from '../store/slices/restaurantSlice';

type EmailAuthScreenNavigationProp = StackNavigationProp<RootStackParamList, 'EmailAuth'>;
type EmailAuthScreenRouteProp = RouteProp<RootStackParamList, 'EmailAuth'>;

export default function EmailAuthScreen() {
    const navigation = useNavigation<EmailAuthScreenNavigationProp>();
    const route = useRoute<EmailAuthScreenRouteProp>();
    const { intent } = route.params;
    const dispatch = useAppDispatch();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [loading, setLoading] = useState(false);

    const isSignup = intent === 'signup';

    const handleAuth = async () => {
        if (!email || !password) {
            Alert.alert('Error', 'Please fill in all fields');
            return;
        }

        if (isSignup && password !== confirmPassword) {
            Alert.alert('Error', 'Passwords do not match');
            return;
        }

        setLoading(true);
        try {
            if (isSignup) {
                await createUserWithEmailAndPassword(auth, email, password);
                Alert.alert('Success', 'Account created successfully!');
                navigation.navigate('Onboarding');
            } else {
                await signInWithEmailAndPassword(auth, email, password);
                // Try to hydrate restaurant data
                try {
                    // @ts-ignore - Dispatching thunk
                    await dispatch(hydrateRestaurant());
                } catch (e) {
                    console.log('Hydration failed (expected if first login on new device):', e);
                }
                navigation.navigate('Main');
            }
        } catch (error: any) {
            console.error('Auth error:', error);
            let errorMessage = 'Authentication failed';
            if (error.code === 'auth/email-already-in-use') {
                errorMessage = 'Email is already in use';
            } else if (error.code === 'auth/invalid-email') {
                errorMessage = 'Invalid email address';
            } else if (error.code === 'auth/weak-password') {
                errorMessage = 'Password should be at least 6 characters';
            } else if (error.code === 'auth/user-not-found' || error.code === 'auth/wrong-password') {
                errorMessage = 'Invalid email or password';
            }
            Alert.alert('Error', errorMessage);
        } finally {
            setLoading(false);
        }
    };

    return (
        <SafeAreaView style={styles.container}>
            <LinearGradient
                colors={['#16a34a', '#15803d']}
                style={styles.gradient}
            >
                <KeyboardAvoidingView
                    behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
                    style={{ flex: 1 }}
                >
                    <ScrollView contentContainerStyle={styles.scrollContent}>
                        <TouchableOpacity
                            style={styles.backButton}
                            onPress={() => navigation.goBack()}
                        >
                            <Ionicons name="arrow-back" size={24} color="white" />
                        </TouchableOpacity>

                        <View style={styles.header}>
                            <Ionicons name={isSignup ? "person-add" : "log-in"} size={64} color="white" />
                            <Text style={styles.title}>
                                {isSignup ? 'Create Account' : 'Welcome Back'}
                            </Text>
                            <Text style={styles.subtitle}>
                                {isSignup
                                    ? 'Sign up to start managing your restaurant'
                                    : 'Sign in to continue to your dashboard'}
                            </Text>
                        </View>

                        <View style={styles.form}>
                            <View style={styles.inputContainer}>
                                <Ionicons name="mail" size={20} color="rgba(255,255,255,0.8)" style={styles.inputIcon} />
                                <TextInput
                                    style={styles.input}
                                    placeholder="Email Address"
                                    placeholderTextColor="rgba(255,255,255,0.6)"
                                    value={email}
                                    onChangeText={setEmail}
                                    keyboardType="email-address"
                                    autoCapitalize="none"
                                />
                            </View>

                            <View style={styles.inputContainer}>
                                <Ionicons name="lock-closed" size={20} color="rgba(255,255,255,0.8)" style={styles.inputIcon} />
                                <TextInput
                                    style={styles.input}
                                    placeholder="Password"
                                    placeholderTextColor="rgba(255,255,255,0.6)"
                                    value={password}
                                    onChangeText={setPassword}
                                    secureTextEntry
                                />
                            </View>

                            {isSignup && (
                                <View style={styles.inputContainer}>
                                    <Ionicons name="lock-closed" size={20} color="rgba(255,255,255,0.8)" style={styles.inputIcon} />
                                    <TextInput
                                        style={styles.input}
                                        placeholder="Confirm Password"
                                        placeholderTextColor="rgba(255,255,255,0.6)"
                                        value={confirmPassword}
                                        onChangeText={setConfirmPassword}
                                        secureTextEntry
                                    />
                                </View>
                            )}

                            <TouchableOpacity
                                style={styles.button}
                                onPress={handleAuth}
                                disabled={loading}
                            >
                                {loading ? (
                                    <ActivityIndicator color="#16a34a" />
                                ) : (
                                    <Text style={styles.buttonText}>
                                        {isSignup ? 'Sign Up' : 'Sign In'}
                                    </Text>
                                )}
                            </TouchableOpacity>
                        </View>
                    </ScrollView>
                </KeyboardAvoidingView>
            </LinearGradient>
        </SafeAreaView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
    },
    gradient: {
        flex: 1,
    },
    scrollContent: {
        flexGrow: 1,
        padding: 24,
    },
    backButton: {
        width: 40,
        height: 40,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: 'rgba(255,255,255,0.2)',
        borderRadius: 20,
        marginTop: 8,
    },
    header: {
        alignItems: 'center',
        marginTop: 40,
        marginBottom: 48,
    },
    title: {
        fontSize: 32,
        fontWeight: 'bold',
        color: 'white',
        marginTop: 16,
        textAlign: 'center',
    },
    subtitle: {
        fontSize: 16,
        color: 'rgba(255,255,255,0.8)',
        textAlign: 'center',
        marginTop: 8,
    },
    form: {
        width: '100%',
    },
    inputContainer: {
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: 'rgba(255,255,255,0.2)',
        borderRadius: 12,
        marginBottom: 16,
        paddingHorizontal: 16,
    },
    inputIcon: {
        marginRight: 12,
    },
    input: {
        flex: 1,
        paddingVertical: 16,
        color: 'white',
        fontSize: 16,
    },
    button: {
        backgroundColor: 'white',
        paddingVertical: 16,
        borderRadius: 24,
        alignItems: 'center',
        marginTop: 16,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.25,
        shadowRadius: 3.84,
        elevation: 5,
    },
    buttonText: {
        color: '#16a34a',
        fontSize: 18,
        fontWeight: '600',
    },
});
