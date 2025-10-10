import React, { useState } from 'react';
import { Card } from './components/ui/card';
import { Button } from './components/ui/button';
import { Badge } from './components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './components/ui/tabs';
import { Switch } from './components/ui/switch';
import { 
  Store, 
  BarChart3, 
  Menu, 
  ShoppingCart, 
  Settings, 
  Plus,
  TrendingUp,
  Clock,
  CheckCircle,
  AlertCircle,
  Star,
  MapPin,
  Phone,
  Mail,
  Upload,
  Camera,
  Utensils,
  Timer,
  Package,
  DollarSign,
  Users,
  Bell,
  Sun,
  Moon,
  MessageCircle,
  Search,
  Filter,
  Edit,
  Eye,
  MoreVertical,
  ArrowRight,
  ChefHat,
  Target,
  Award,
  Zap
} from 'lucide-react';
import { WelcomeScreen } from './components/welcome-screen';
import { OnboardingFlow } from './components/onboarding-flow';
import { Dashboard } from './components/dashboard';
import { MenuManagement } from './components/menu-management';
import { OrderManagement } from './components/order-management';
import { ProfileSettings } from './components/profile-settings';

type Screen = 'welcome' | 'onboarding' | 'dashboard' | 'menu' | 'orders' | 'profile';

export default function App() {
  const [currentScreen, setCurrentScreen] = useState<Screen>('welcome');
  const [isDarkMode, setIsDarkMode] = useState(false);

  const toggleDarkMode = () => {
    setIsDarkMode(!isDarkMode);
    document.documentElement.classList.toggle('dark');
  };

  const renderCurrentScreen = () => {
    switch (currentScreen) {
      case 'welcome':
        return <WelcomeScreen onNext={() => setCurrentScreen('onboarding')} />;
      case 'onboarding':
        return <OnboardingFlow onComplete={() => setCurrentScreen('dashboard')} />;
      case 'dashboard':
        return <Dashboard />;
      case 'menu':
        return <MenuManagement />;
      case 'orders':
        return <OrderManagement />;
      case 'profile':
        return <ProfileSettings />;
      default:
        return <Dashboard />;
    }
  };

  const isMainApp = !['welcome', 'onboarding'].includes(currentScreen);

  if (!isMainApp) {
    return (
      <div className={`min-h-screen ${isDarkMode ? 'dark' : ''}`}>
        <div className="min-h-screen bg-background">
          {renderCurrentScreen()}
        </div>
      </div>
    );
  }

  return (
    <div className={`min-h-screen ${isDarkMode ? 'dark' : ''}`}>
      <div className="min-h-screen bg-background flex">
        {/* Sidebar Navigation */}
        <div className="w-64 bg-card border-r border-border">
          <div className="p-6">
            <div className="flex items-center gap-3 mb-8">
              <div className="w-10 h-10 bg-gradient-to-r from-[#16a34a] to-[#15803d] rounded-xl flex items-center justify-center">
                <ChefHat className="w-6 h-6 text-white" />
              </div>
              <div>
                <h1 className="text-xl font-bold text-[#16a34a]">Nashtto</h1>
                <p className="text-sm text-muted-foreground">Partner App</p>
              </div>
            </div>

            <nav className="space-y-2">
              <Button
                variant={currentScreen === 'dashboard' ? 'default' : 'ghost'}
                className="w-full justify-start gap-3"
                onClick={() => setCurrentScreen('dashboard')}
              >
                <BarChart3 className="w-5 h-5" />
                Dashboard
              </Button>
              <Button
                variant={currentScreen === 'menu' ? 'default' : 'ghost'}
                className="w-full justify-start gap-3"
                onClick={() => setCurrentScreen('menu')}
              >
                <Menu className="w-5 h-5" />
                Menu Management
              </Button>
              <Button
                variant={currentScreen === 'orders' ? 'default' : 'ghost'}
                className="w-full justify-start gap-3"
                onClick={() => setCurrentScreen('orders')}
              >
                <ShoppingCart className="w-5 h-5" />
                Orders
              </Button>
              <Button
                variant={currentScreen === 'profile' ? 'default' : 'ghost'}
                className="w-full justify-start gap-3"
                onClick={() => setCurrentScreen('profile')}
              >
                <Settings className="w-5 h-5" />
                Settings
              </Button>
            </nav>
          </div>

          <div className="absolute bottom-6 left-6 right-6">
            <div className="flex items-center justify-between">
              <span className="text-sm text-muted-foreground">Dark Mode</span>
              <Switch
                checked={isDarkMode}
                onCheckedChange={toggleDarkMode}
              />
            </div>
          </div>
        </div>

        {/* Main Content */}
        <div className="flex-1 overflow-hidden">
          {renderCurrentScreen()}
        </div>
      </div>
    </div>
  );
}