import React, { useState } from 'react';
import { Button } from './ui/button';
import { Card } from './ui/card';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Textarea } from './ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Badge } from './ui/badge';
import { Progress } from './ui/progress';
import { 
  Upload, 
  Camera, 
  MapPin, 
  Clock, 
  FileText, 
  CheckCircle, 
  ArrowRight, 
  ArrowLeft,
  Store,
  Utensils,
  Phone,
  Mail,
  CreditCard,
  Shield,
  Sparkles,
  Eye,
  Edit
} from 'lucide-react';

interface OnboardingFlowProps {
  onComplete: () => void;
}

export function OnboardingFlow({ onComplete }: OnboardingFlowProps) {
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({
    restaurantName: '',
    cuisineType: '',
    address: '',
    phone: '',
    email: '',
    gstNumber: '',
    fssaiLicense: '',
    operatingHours: {
      open: '09:00',
      close: '22:00'
    }
  });

  const totalSteps = 5;
  const progress = (step / totalSteps) * 100;

  const nextStep = () => {
    if (step < totalSteps) {
      setStep(step + 1);
    } else {
      onComplete();
    }
  };

  const prevStep = () => {
    if (step > 1) {
      setStep(step - 1);
    }
  };

  const stepTitles = [
    'Restaurant Details',
    'Contact Information',
    'License & Registration',
    'Logo & Photos',
    'Menu Upload',
    'Review & Submit'
  ];

  const renderStep = () => {
    switch (step) {
      case 1:
        return (
          <div className="space-y-6">
            <div className="text-center mb-8">
              <Store className="w-12 h-12 text-[#16a34a] mx-auto mb-4" />
              <h2 className="text-2xl font-bold">Tell us about your restaurant</h2>
              <p className="text-muted-foreground">Basic information to get you started</p>
            </div>

            <div className="space-y-4">
              <div>
                <Label htmlFor="restaurantName">Restaurant Name</Label>
                <Input
                  id="restaurantName"
                  placeholder="Enter your restaurant name"
                  value={formData.restaurantName}
                  onChange={(e) => setFormData({...formData, restaurantName: e.target.value})}
                />
              </div>

              <div>
                <Label htmlFor="cuisineType">Cuisine Type</Label>
                <Select value={formData.cuisineType} onValueChange={(value) => setFormData({...formData, cuisineType: value})}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select cuisine type" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="indian">Indian</SelectItem>
                    <SelectItem value="chinese">Chinese</SelectItem>
                    <SelectItem value="italian">Italian</SelectItem>
                    <SelectItem value="mexican">Mexican</SelectItem>
                    <SelectItem value="american">American</SelectItem>
                    <SelectItem value="continental">Continental</SelectItem>
                    <SelectItem value="fast-food">Fast Food</SelectItem>
                    <SelectItem value="desserts">Desserts</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div>
                <Label htmlFor="address">Restaurant Address</Label>
                <Textarea
                  id="address"
                  placeholder="Enter complete address with pincode"
                  value={formData.address}
                  onChange={(e) => setFormData({...formData, address: e.target.value})}
                />
                <div className="flex items-center gap-2 mt-2 text-sm text-muted-foreground">
                  <MapPin className="w-4 h-4" />
                  <span>We'll help customers find you easily</span>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="openTime">Opening Time</Label>
                  <Input
                    id="openTime"
                    type="time"
                    value={formData.operatingHours.open}
                    onChange={(e) => setFormData({
                      ...formData, 
                      operatingHours: {...formData.operatingHours, open: e.target.value}
                    })}
                  />
                </div>
                <div>
                  <Label htmlFor="closeTime">Closing Time</Label>
                  <Input
                    id="closeTime"
                    type="time"
                    value={formData.operatingHours.close}
                    onChange={(e) => setFormData({
                      ...formData, 
                      operatingHours: {...formData.operatingHours, close: e.target.value}
                    })}
                  />
                </div>
              </div>
            </div>
          </div>
        );

      case 2:
        return (
          <div className="space-y-6">
            <div className="text-center mb-8">
              <Phone className="w-12 h-12 text-[#16a34a] mx-auto mb-4" />
              <h2 className="text-2xl font-bold">Contact Information</h2>
              <p className="text-muted-foreground">How customers and our team can reach you</p>
            </div>

            <div className="space-y-4">
              <div>
                <Label htmlFor="phone">Phone Number</Label>
                <Input
                  id="phone"
                  placeholder="+91 98765 43210"
                  value={formData.phone}
                  onChange={(e) => setFormData({...formData, phone: e.target.value})}
                />
              </div>

              <div>
                <Label htmlFor="email">Email Address</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="restaurant@example.com"
                  value={formData.email}
                  onChange={(e) => setFormData({...formData, email: e.target.value})}
                />
              </div>
            </div>
          </div>
        );

      case 3:
        return (
          <div className="space-y-6">
            <div className="text-center mb-8">
              <Shield className="w-12 h-12 text-[#16a34a] mx-auto mb-4" />
              <h2 className="text-2xl font-bold">License & Registration</h2>
              <p className="text-muted-foreground">Required documents for compliance</p>
            </div>

            <div className="space-y-4">
              <div>
                <Label htmlFor="gst">GST Number</Label>
                <Input
                  id="gst"
                  placeholder="22AAAAA0000A1Z5"
                  value={formData.gstNumber}
                  onChange={(e) => setFormData({...formData, gstNumber: e.target.value})}
                />
              </div>

              <div>
                <Label htmlFor="fssai">FSSAI License Number</Label>
                <Input
                  id="fssai"
                  placeholder="12345678901234"
                  value={formData.fssaiLicense}
                  onChange={(e) => setFormData({...formData, fssaiLicense: e.target.value})}
                />
              </div>

              <div className="bg-blue-50 p-4 rounded-xl border border-blue-200">
                <div className="flex items-start gap-3">
                  <Shield className="w-5 h-5 text-blue-600 mt-0.5" />
                  <div>
                    <h4 className="font-medium text-blue-900">Secure & Verified</h4>
                    <p className="text-sm text-blue-700">
                      Your documents are encrypted and stored securely. We only use them for verification purposes.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );

      case 4:
        return (
          <div className="space-y-6">
            <div className="text-center mb-8">
              <Camera className="w-12 h-12 text-[#16a34a] mx-auto mb-4" />
              <h2 className="text-2xl font-bold">Upload Photos</h2>
              <p className="text-muted-foreground">Make your restaurant look appetizing</p>
            </div>

            <div className="space-y-6">
              <div>
                <Label>Restaurant Logo</Label>
                <div className="border-2 border-dashed border-border rounded-xl p-8 text-center hover:border-[#16a34a] transition-colors cursor-pointer">
                  <Upload className="w-8 h-8 text-muted-foreground mx-auto mb-2" />
                  <p className="font-medium">Click to upload your logo</p>
                  <p className="text-sm text-muted-foreground">PNG, JPG up to 2MB</p>
                </div>
              </div>

              <div>
                <Label>Cover Photo</Label>
                <div className="border-2 border-dashed border-border rounded-xl p-8 text-center hover:border-[#16a34a] transition-colors cursor-pointer">
                  <Camera className="w-8 h-8 text-muted-foreground mx-auto mb-2" />
                  <p className="font-medium">Upload restaurant cover photo</p>
                  <p className="text-sm text-muted-foreground">High-quality image of your restaurant</p>
                </div>
              </div>
            </div>
          </div>
        );

      case 5:
        return (
          <div className="space-y-6">
            <div className="text-center mb-8">
              <Sparkles className="w-12 h-12 text-[#16a34a] mx-auto mb-4" />
              <h2 className="text-2xl font-bold">Menu Upload</h2>
              <p className="text-muted-foreground">Our AI will extract your menu items automatically</p>
            </div>

            <div className="space-y-6">
              <div className="bg-gradient-to-r from-[#f0fdf4] to-white p-6 rounded-xl border">
                <div className="flex items-center gap-3 mb-4">
                  <Sparkles className="w-6 h-6 text-[#16a34a]" />
                  <h3 className="font-semibold">AI Menu Extraction</h3>
                </div>
                <p className="text-sm text-muted-foreground mb-4">
                  Upload a photo or PDF of your menu, and our AI will automatically extract all items, prices, and descriptions. You can review and edit them before publishing.
                </p>
                <div className="flex flex-wrap gap-2">
                  <Badge variant="secondary">✓ Automatic item detection</Badge>
                  <Badge variant="secondary">✓ Price extraction</Badge>
                  <Badge variant="secondary">✓ Category sorting</Badge>
                </div>
              </div>

              <div className="border-2 border-dashed border-border rounded-xl p-8 text-center hover:border-[#16a34a] transition-colors cursor-pointer">
                <FileText className="w-8 h-8 text-muted-foreground mx-auto mb-2" />
                <p className="font-medium">Upload Menu (Photo/PDF)</p>
                <p className="text-sm text-muted-foreground">Clear photo or PDF of your current menu</p>
              </div>

              <div className="bg-amber-50 p-4 rounded-xl border border-amber-200">
                <div className="flex items-start gap-3">
                  <Eye className="w-5 h-5 text-amber-600 mt-0.5" />
                  <div>
                    <h4 className="font-medium text-amber-900">Review Before Publishing</h4>
                    <p className="text-sm text-amber-700">
                      You'll be able to review and edit all extracted items before they go live on your menu.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#fff4f1] to-white flex items-center justify-center p-4">
      <div className="max-w-2xl w-full">
        <Card className="p-8">
          <div className="mb-8">
            <div className="flex items-center justify-between mb-4">
              <h1 className="text-lg font-semibold">Restaurant Onboarding</h1>
              <span className="text-sm text-muted-foreground">Step {step} of {totalSteps}</span>
            </div>
            <Progress value={progress} className="h-2" />
          </div>

          {renderStep()}

          <div className="flex justify-between pt-8 mt-8 border-t">
            <Button
              variant="outline"
              onClick={prevStep}
              disabled={step === 1}
              className="flex items-center gap-2"
            >
              <ArrowLeft className="w-4 h-4" />
              Previous
            </Button>

            <Button
              onClick={nextStep}
              className="flex items-center gap-2 bg-[#16a34a] hover:bg-[#15803d]"
            >
              {step === totalSteps ? 'Complete Setup' : 'Next'}
              <ArrowRight className="w-4 h-4" />
            </Button>
          </div>
        </Card>
      </div>
    </div>
  );
}