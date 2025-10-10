import React from 'react';
import { Button } from './ui/button';
import { Card } from './ui/card';
import { ChefHat, Store, BarChart3, Clock, ArrowRight, Star, Shield, Zap } from 'lucide-react';
import { ImageWithFallback } from './figma/ImageWithFallback';

interface WelcomeScreenProps {
  onNext: () => void;
}

export function WelcomeScreen({ onNext }: WelcomeScreenProps) {
  return (
    <div className="min-h-screen bg-gradient-to-br from-[#fff4f1] to-white flex items-center justify-center p-4">
      <div className="max-w-4xl w-full">
        <div className="text-center mb-12">
          <div className="flex items-center justify-center gap-3 mb-6">
            <div className="w-16 h-16 bg-gradient-to-r from-[#16a34a] to-[#15803d] rounded-2xl flex items-center justify-center">
              <ChefHat className="w-8 h-8 text-white" />
            </div>
            <div>
              <h1 className="text-4xl font-bold text-[#16a34a]">Nashtto</h1>
              <p className="text-lg text-muted-foreground">Partner App</p>
            </div>
          </div>
          
          <h2 className="text-3xl font-bold text-foreground mb-4">
            Welcome to Your Digital Kitchen
          </h2>
          <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
            Streamline your restaurant operations, manage orders efficiently, and grow your business with our powerful partner platform.
          </p>
        </div>

        <div className="grid md:grid-cols-3 gap-6 mb-12">
          <Card className="p-6 text-center hover:shadow-lg transition-shadow">
            <div className="w-12 h-12 bg-[#f0fdf4] rounded-xl flex items-center justify-center mx-auto mb-4">
              <Store className="w-6 h-6 text-[#16a34a]" />
            </div>
            <h3 className="font-semibold mb-2">Easy Setup</h3>
            <p className="text-sm text-muted-foreground">
              Get your restaurant online in minutes with our simple onboarding process.
            </p>
          </Card>

          <Card className="p-6 text-center hover:shadow-lg transition-shadow">
            <div className="w-12 h-12 bg-[#f0fdf4] rounded-xl flex items-center justify-center mx-auto mb-4">
              <BarChart3 className="w-6 h-6 text-[#16a34a]" />
            </div>
            <h3 className="font-semibold mb-2">Real-time Analytics</h3>
            <p className="text-sm text-muted-foreground">
              Track your sales, popular dishes, and customer insights in real-time.
            </p>
          </Card>

          <Card className="p-6 text-center hover:shadow-lg transition-shadow">
            <div className="w-12 h-12 bg-[#f0fdf4] rounded-xl flex items-center justify-center mx-auto mb-4">
              <Clock className="w-6 h-6 text-[#16a34a]" />
            </div>
            <h3 className="font-semibold mb-2">Efficient Orders</h3>
            <p className="text-sm text-muted-foreground">
              Manage incoming orders seamlessly with automated workflows.
            </p>
          </Card>
        </div>

        <div className="grid md:grid-cols-3 gap-4 mb-12">
          <div className="flex items-center gap-3 p-4 bg-card rounded-xl border">
            <Star className="w-5 h-5 text-[#16a34a]" />
            <span className="text-sm">AI-Powered Insights</span>
          </div>
          <div className="flex items-center gap-3 p-4 bg-card rounded-xl border">
            <Shield className="w-5 h-5 text-[#16a34a]" />
            <span className="text-sm">Secure & Reliable</span>
          </div>
          <div className="flex items-center gap-3 p-4 bg-card rounded-xl border">
            <Zap className="w-5 h-5 text-[#16a34a]" />
            <span className="text-sm">Lightning Fast</span>
          </div>
        </div>

        <div className="text-center">
          <Button onClick={onNext} size="lg" className="px-8 py-3 bg-[#16a34a] hover:bg-[#15803d]">
            Get Started
            <ArrowRight className="w-5 h-5 ml-2" />
          </Button>
          <p className="text-sm text-muted-foreground mt-4">
            Join 10,000+ restaurants already using Nashtto
          </p>
        </div>
      </div>
    </div>
  );
}