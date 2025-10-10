import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Textarea } from './ui/textarea';
import { Switch } from './ui/switch';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { Avatar, AvatarFallback, AvatarImage } from './ui/avatar';
import { Badge } from './ui/badge';
import { Separator } from './ui/separator';
import { 
  Store, 
  Phone, 
  Mail, 
  MapPin, 
  Clock, 
  CreditCard,
  Users,
  Bell,
  Shield,
  HelpCircle,
  MessageCircle,
  Camera,
  Edit,
  Plus,
  Trash2,
  Settings,
  Star,
  TrendingUp,
  DollarSign,
  Calendar,
  Download,
  Eye,
  CheckCircle,
  AlertTriangle
} from 'lucide-react';

export function ProfileSettings() {
  const [activeTab, setActiveTab] = useState('restaurant');

  const restaurantInfo = {
    name: 'Spice Garden Restaurant',
    cuisine: 'Indian, Continental',
    address: '123, MG Road, Sector 14, Gurgaon, Haryana 122001',
    phone: '+91 98765 43210',
    email: 'contact@spicegarden.com',
    gst: '07AAAAA0000A1Z5',
    fssai: '12345678901234',
    operatingHours: {
      open: '09:00',
      close: '23:00'
    },
    status: 'active',
    rating: 4.5,
    totalOrders: 1247,
    joinDate: 'March 2023'
  };

  const staffMembers = [
    {
      id: 1,
      name: 'Rajesh Kumar',
      role: 'Manager',
      email: 'rajesh@spicegarden.com',
      phone: '+91 87654 32109',
      status: 'active',
      permissions: ['orders', 'menu', 'reports']
    },
    {
      id: 2,
      name: 'Priya Sharma',
      role: 'Chef',
      email: 'priya@spicegarden.com',
      phone: '+91 76543 21098',
      status: 'active',
      permissions: ['orders', 'menu']
    },
    {
      id: 3,
      name: 'Amit Singh',
      role: 'Staff',
      email: 'amit@spicegarden.com',
      phone: '+91 65432 10987',
      status: 'inactive',
      permissions: ['orders']
    }
  ];

  const notifications = {
    orderAlerts: true,
    emailNotifications: true,
    smsNotifications: false,
    promotionUpdates: true,
    systemMaintenance: true
  };

  const payoutSummary = {
    totalEarnings: 45650,
    pendingAmount: 2340,
    lastPayout: '₹4,500 on 15th March',
    nextPayout: '22nd March',
    bankAccount: '**** **** **** 1234'
  };

  return (
    <div className="p-6 space-y-6 max-h-screen overflow-y-auto">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Restaurant Settings</h1>
          <p className="text-muted-foreground">Manage your restaurant profile and preferences</p>
        </div>
        <Button className="gap-2 bg-[#ff6b35] hover:bg-[#e55a2e]">
          <Download className="w-4 h-4" />
          Export Data
        </Button>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-5">
          <TabsTrigger value="restaurant">Restaurant</TabsTrigger>
          <TabsTrigger value="staff">Staff</TabsTrigger>
          <TabsTrigger value="notifications">Notifications</TabsTrigger>
          <TabsTrigger value="payments">Payments</TabsTrigger>
          <TabsTrigger value="support">Support</TabsTrigger>
        </TabsList>

        {/* Restaurant Info Tab */}
        <TabsContent value="restaurant" className="space-y-6">
          <div className="grid lg:grid-cols-3 gap-6">
            <Card className="lg:col-span-2">
              <CardHeader>
                <CardTitle>Restaurant Information</CardTitle>
                <CardDescription>Update your restaurant details</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="restaurantName">Restaurant Name</Label>
                    <Input id="restaurantName" defaultValue={restaurantInfo.name} />
                  </div>
                  <div>
                    <Label htmlFor="cuisine">Cuisine Type</Label>
                    <Input id="cuisine" defaultValue={restaurantInfo.cuisine} />
                  </div>
                </div>

                <div>
                  <Label htmlFor="address">Address</Label>
                  <Textarea id="address" defaultValue={restaurantInfo.address} />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="phone">Phone Number</Label>
                    <Input id="phone" defaultValue={restaurantInfo.phone} />
                  </div>
                  <div>
                    <Label htmlFor="email">Email Address</Label>
                    <Input id="email" defaultValue={restaurantInfo.email} />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="openTime">Opening Time</Label>
                    <Input id="openTime" type="time" defaultValue={restaurantInfo.operatingHours.open} />
                  </div>
                  <div>
                    <Label htmlFor="closeTime">Closing Time</Label>
                    <Input id="closeTime" type="time" defaultValue={restaurantInfo.operatingHours.close} />
                  </div>
                </div>

                <Separator />

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <Label htmlFor="gst">GST Number</Label>
                    <Input id="gst" defaultValue={restaurantInfo.gst} />
                  </div>
                  <div>
                    <Label htmlFor="fssai">FSSAI License</Label>
                    <Input id="fssai" defaultValue={restaurantInfo.fssai} />
                  </div>
                </div>

                <Button className="bg-[#ff6b35] hover:bg-[#e55a2e]">Save Changes</Button>
              </CardContent>
            </Card>

            <div className="space-y-6">
              <Card>
                <CardHeader>
                  <CardTitle>Restaurant Status</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span>Currently</span>
                    <Badge className="bg-green-100 text-green-800">
                      <CheckCircle className="w-3 h-3 mr-1" />
                      Active
                    </Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span>Rating</span>
                    <div className="flex items-center gap-1">
                      <Star className="w-4 h-4 text-yellow-500 fill-current" />
                      <span className="font-medium">{restaurantInfo.rating}</span>
                    </div>
                  </div>
                  <div className="flex items-center justify-between">
                    <span>Total Orders</span>
                    <span className="font-medium">{restaurantInfo.totalOrders}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span>Member Since</span>
                    <span className="font-medium">{restaurantInfo.joinDate}</span>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Restaurant Photos</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <div className="border-2 border-dashed border-border rounded-lg p-4 text-center cursor-pointer hover:border-[#ff6b35]">
                      <Camera className="w-6 h-6 text-muted-foreground mx-auto mb-2" />
                      <p className="text-sm">Update Logo</p>
                    </div>
                    <div className="border-2 border-dashed border-border rounded-lg p-4 text-center cursor-pointer hover:border-[#ff6b35]">
                      <Camera className="w-6 h-6 text-muted-foreground mx-auto mb-2" />
                      <p className="text-sm">Update Cover Photo</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </TabsContent>

        {/* Staff Management Tab */}
        <TabsContent value="staff" className="space-y-6">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <div>
                  <CardTitle>Staff Management</CardTitle>
                  <CardDescription>Manage your team members and their permissions</CardDescription>
                </div>
                <Button className="gap-2 bg-[#ff6b35] hover:bg-[#e55a2e]">
                  <Plus className="w-4 h-4" />
                  Add Staff Member
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {staffMembers.map((member) => (
                  <div key={member.id} className="flex items-center justify-between p-4 border rounded-xl">
                    <div className="flex items-center gap-4">
                      <Avatar className="w-12 h-12">
                        <AvatarFallback>{member.name.split(' ').map(n => n[0]).join('')}</AvatarFallback>
                      </Avatar>
                      <div>
                        <div className="flex items-center gap-3">
                          <h4 className="font-medium">{member.name}</h4>
                          <Badge variant="outline">{member.role}</Badge>
                          <Badge className={member.status === 'active' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}>
                            {member.status}
                          </Badge>
                        </div>
                        <p className="text-sm text-muted-foreground">{member.email}</p>
                        <div className="flex gap-1 mt-1">
                          {member.permissions.map((permission) => (
                            <Badge key={permission} variant="secondary" className="text-xs">
                              {permission}
                            </Badge>
                          ))}
                        </div>
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <Button variant="outline" size="sm">
                        <Edit className="w-3 h-3" />
                      </Button>
                      <Button variant="outline" size="sm">
                        <Trash2 className="w-3 h-3" />
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Notifications Tab */}
        <TabsContent value="notifications" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>Notification Preferences</CardTitle>
              <CardDescription>Choose how you want to receive updates</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="font-medium">Order Alerts</h4>
                  <p className="text-sm text-muted-foreground">Get notified about new orders</p>
                </div>
                <Switch defaultChecked={notifications.orderAlerts} />
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <h4 className="font-medium">Email Notifications</h4>
                  <p className="text-sm text-muted-foreground">Receive updates via email</p>
                </div>
                <Switch defaultChecked={notifications.emailNotifications} />
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <h4 className="font-medium">SMS Notifications</h4>
                  <p className="text-sm text-muted-foreground">Get SMS alerts for urgent updates</p>
                </div>
                <Switch defaultChecked={notifications.smsNotifications} />
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <h4 className="font-medium">Promotion Updates</h4>
                  <p className="text-sm text-muted-foreground">Learn about new features and promotions</p>
                </div>
                <Switch defaultChecked={notifications.promotionUpdates} />
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <h4 className="font-medium">System Maintenance</h4>
                  <p className="text-sm text-muted-foreground">Important system updates and maintenance</p>
                </div>
                <Switch defaultChecked={notifications.systemMaintenance} />
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Payments Tab */}
        <TabsContent value="payments" className="space-y-6">
          <div className="grid lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle>Payout Summary</CardTitle>
                <CardDescription>Your earnings and payout details</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="p-4 bg-green-50 rounded-xl">
                    <div className="flex items-center gap-2 mb-2">
                      <DollarSign className="w-4 h-4 text-green-600" />
                      <span className="text-sm text-green-800">Total Earnings</span>
                    </div>
                    <p className="text-2xl font-bold text-green-900">₹{payoutSummary.totalEarnings.toLocaleString()}</p>
                  </div>
                  <div className="p-4 bg-orange-50 rounded-xl">
                    <div className="flex items-center gap-2 mb-2">
                      <Clock className="w-4 h-4 text-orange-600" />
                      <span className="text-sm text-orange-800">Pending</span>
                    </div>
                    <p className="text-2xl font-bold text-orange-900">₹{payoutSummary.pendingAmount.toLocaleString()}</p>
                  </div>
                </div>

                <Separator />

                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">Last Payout</span>
                    <span className="font-medium">{payoutSummary.lastPayout}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">Next Payout</span>
                    <span className="font-medium">{payoutSummary.nextPayout}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">Bank Account</span>
                    <span className="font-medium">{payoutSummary.bankAccount}</span>
                  </div>
                </div>

                <Button variant="outline" className="w-full">
                  <Eye className="w-4 h-4 mr-2" />
                  View Payout History
                </Button>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Bank Details</CardTitle>
                <CardDescription>Update your bank information for payouts</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div>
                  <Label htmlFor="accountHolder">Account Holder Name</Label>
                  <Input id="accountHolder" placeholder="Enter account holder name" />
                </div>
                <div>
                  <Label htmlFor="accountNumber">Account Number</Label>
                  <Input id="accountNumber" placeholder="Enter account number" />
                </div>
                <div>
                  <Label htmlFor="ifsc">IFSC Code</Label>
                  <Input id="ifsc" placeholder="Enter IFSC code" />
                </div>
                <div>
                  <Label htmlFor="bankName">Bank Name</Label>
                  <Input id="bankName" placeholder="Enter bank name" />
                </div>
                <Button className="w-full bg-[#ff6b35] hover:bg-[#e55a2e]">
                  Update Bank Details
                </Button>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* Support Tab */}
        <TabsContent value="support" className="space-y-6">
          <div className="grid lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle>Help & Support</CardTitle>
                <CardDescription>Get help when you need it</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <Button variant="outline" className="w-full justify-start gap-3">
                  <MessageCircle className="w-4 h-4" />
                  Start Live Chat
                </Button>
                <Button variant="outline" className="w-full justify-start gap-3">
                  <Phone className="w-4 h-4" />
                  Call Support: 1800-123-4567
                </Button>
                <Button variant="outline" className="w-full justify-start gap-3">
                  <Mail className="w-4 h-4" />
                  Email: support@nashtto.com
                </Button>
                <Button variant="outline" className="w-full justify-start gap-3">
                  <HelpCircle className="w-4 h-4" />
                  FAQ & Help Center
                </Button>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Quick Actions</CardTitle>
                <CardDescription>Common tasks and shortcuts</CardDescription>
              </CardHeader>
              <CardContent className="space-y-3">
                <Button variant="outline" className="w-full justify-start">
                  Download User Manual
                </Button>
                <Button variant="outline" className="w-full justify-start">
                  Submit Feedback
                </Button>
                <Button variant="outline" className="w-full justify-start">
                  Report an Issue
                </Button>
                <Button variant="outline" className="w-full justify-start">
                  Request Feature
                </Button>
              </CardContent>
            </Card>
          </div>

          <Card>
            <CardHeader>
              <CardTitle>System Information</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                <div>
                  <span className="text-muted-foreground">App Version:</span>
                  <span className="ml-2 font-medium">2.1.0</span>
                </div>
                <div>
                  <span className="text-muted-foreground">Last Updated:</span>
                  <span className="ml-2 font-medium">March 15, 2024</span>
                </div>
                <div>
                  <span className="text-muted-foreground">Status:</span>
                  <Badge className="ml-2 bg-green-100 text-green-800">
                    <CheckCircle className="w-3 h-3 mr-1" />
                    All Systems Operational
                  </Badge>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}