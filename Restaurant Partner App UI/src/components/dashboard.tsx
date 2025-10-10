import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Progress } from './ui/progress';
import { 
  IndianRupee, 
  TrendingUp, 
  ShoppingCart, 
  Clock, 
  Plus,
  Eye,
  Star,
  Utensils,
  Users,
  Target,
  Award,
  Zap,
  ArrowUp,
  ArrowDown,
  Calendar,
  BarChart3,
  PieChart
} from 'lucide-react';

export function Dashboard() {
  const quickStats = [
    {
      title: 'Today\'s Revenue',
      value: '₹12,450',
      change: '+12.5%',
      trend: 'up',
      icon: IndianRupee,
      color: 'text-green-600'
    },
    {
      title: 'Orders Today',
      value: '47',
      change: '+8.2%',
      trend: 'up',
      icon: ShoppingCart,
      color: 'text-blue-600'
    },
    {
      title: 'Avg. Order Value',
      value: '₹264',
      change: '-2.1%',
      trend: 'down',
      icon: TrendingUp,
      color: 'text-orange-600'
    },
    {
      title: 'Active Items',
      value: '23',
      change: '2 new',
      trend: 'up',
      icon: Utensils,
      color: 'text-purple-600'
    }
  ];

  const recentOrders = [
    { id: '#1234', customer: 'Rahul S.', items: 3, total: '₹420', status: 'preparing', time: '2 min ago' },
    { id: '#1235', customer: 'Priya M.', items: 2, total: '₹290', status: 'ready', time: '5 min ago' },
    { id: '#1236', customer: 'Amit K.', items: 1, total: '₹150', status: 'delivered', time: '12 min ago' },
    { id: '#1237', customer: 'Sneha R.', items: 4, total: '₹580', status: 'new', time: '15 min ago' }
  ];

  const topItems = [
    { name: 'Paneer Tikka Roll', orders: 12, revenue: '₹1,800' },
    { name: 'Chicken Biryani', orders: 8, revenue: '₹1,600' },
    { name: 'Masala Dosa', orders: 6, revenue: '₹900' },
    { name: 'Veg Thali', orders: 5, revenue: '₹1,250' }
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'new': return 'bg-blue-100 text-blue-800';
      case 'preparing': return 'bg-orange-100 text-orange-800';
      case 'ready': return 'bg-green-100 text-green-800';
      case 'delivered': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="p-6 space-y-6 max-h-screen overflow-y-auto">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Good Morning, Chef! 👋</h1>
          <p className="text-muted-foreground">Here's what's happening at your restaurant today</p>
        </div>
        <div className="flex gap-3">
          <Button variant="outline" className="gap-2">
            <Calendar className="w-4 h-4" />
            Today
          </Button>
          <Button className="gap-2 bg-[#16a34a] hover:bg-[#15803d]">
            <Plus className="w-4 h-4" />
            Add Menu Item
          </Button>
        </div>
      </div>

      {/* Quick Stats */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {quickStats.map((stat, index) => (
          <Card key={index} className="hover:shadow-lg transition-shadow">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-muted-foreground">{stat.title}</p>
                  <p className="text-2xl font-bold">{stat.value}</p>
                  <div className="flex items-center gap-1 mt-1">
                    {stat.trend === 'up' ? (
                      <ArrowUp className="w-3 h-3 text-green-600" />
                    ) : (
                      <ArrowDown className="w-3 h-3 text-red-600" />
                    )}
                    <span className={`text-xs ${stat.trend === 'up' ? 'text-green-600' : 'text-red-600'}`}>
                      {stat.change}
                    </span>
                  </div>
                </div>
                <div className={`w-12 h-12 rounded-xl bg-[#f0fdf4] flex items-center justify-center`}>
                  <stat.icon className={`w-6 h-6 ${stat.color}`} />
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid lg:grid-cols-3 gap-6">
        {/* AI Insights Card */}
        <Card className="lg:col-span-2 bg-gradient-to-r from-[#f0fdf4] to-white border-[#16a34a]/20">
          <CardHeader>
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-[#16a34a] rounded-xl flex items-center justify-center">
                <Zap className="w-5 h-5 text-white" />
              </div>
              <div>
                <CardTitle>AI Insights</CardTitle>
                <CardDescription>Personalized recommendations for your restaurant</CardDescription>
              </div>
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-start gap-3 p-4 bg-white rounded-xl border">
              <Target className="w-5 h-5 text-[#16a34a] mt-0.5" />
              <div>
                <h4 className="font-medium">Peak Hour Analysis</h4>
                <p className="text-sm text-muted-foreground">
                  Your busiest time is 7-9 PM. Consider offering express combos during this window.
                </p>
              </div>
            </div>
            <div className="flex items-start gap-3 p-4 bg-white rounded-xl border">
              <Award className="w-5 h-5 text-[#16a34a] mt-0.5" />
              <div>
                <h4 className="font-medium">Top Performer</h4>
                <p className="text-sm text-muted-foreground">
                  Paneer Tikka Roll is your star dish this week! Consider creating similar items.
                </p>
              </div>
            </div>
            <div className="flex items-start gap-3 p-4 bg-white rounded-xl border">
              <Users className="w-5 h-5 text-[#16a34a] mt-0.5" />
              <div>
                <h4 className="font-medium">Customer Preference</h4>
                <p className="text-sm text-muted-foreground">
                  75% of customers prefer medium spice level. Update your default spice recommendations.
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Quick Actions */}
        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
            <CardDescription>Manage your restaurant efficiently</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <Button className="w-full justify-start gap-3 bg-[#16a34a] hover:bg-[#15803d]">
              <Plus className="w-4 h-4" />
              Add New Item
            </Button>
            <Button variant="outline" className="w-full justify-start gap-3">
              <Clock className="w-4 h-4" />
              Update Hours
            </Button>
            <Button variant="outline" className="w-full justify-start gap-3">
              <Eye className="w-4 h-4" />
              View Menu
            </Button>
            <Button variant="outline" className="w-full justify-start gap-3">
              <BarChart3 className="w-4 h-4" />
              Sales Report
            </Button>
          </CardContent>
        </Card>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        {/* Recent Orders */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle>Recent Orders</CardTitle>
                <CardDescription>Latest customer orders</CardDescription>
              </div>
              <Button variant="outline" size="sm">View All</Button>
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {recentOrders.map((order, index) => (
                <div key={index} className="flex items-center justify-between p-3 border rounded-xl hover:bg-muted/50 transition-colors">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-[#f0fdf4] rounded-full flex items-center justify-center">
                      <ShoppingCart className="w-4 h-4 text-[#16a34a]" />
                    </div>
                    <div>
                      <p className="font-medium">{order.id} - {order.customer}</p>
                      <p className="text-sm text-muted-foreground">{order.items} items • {order.time}</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-medium">{order.total}</p>
                    <Badge className={`text-xs ${getStatusColor(order.status)}`}>
                      {order.status}
                    </Badge>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* Top Performing Items */}
        <Card>
          <CardHeader>
            <CardTitle>Top Performing Items</CardTitle>
            <CardDescription>Your bestsellers today</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {topItems.map((item, index) => (
                <div key={index} className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 bg-[#f0fdf4] rounded-lg flex items-center justify-center">
                      <span className="text-sm font-bold text-[#16a34a]">#{index + 1}</span>
                    </div>
                    <div>
                      <p className="font-medium">{item.name}</p>
                      <p className="text-sm text-muted-foreground">{item.orders} orders</p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-medium">{item.revenue}</p>
                    <div className="flex items-center gap-1">
                      <Star className="w-3 h-3 text-yellow-500 fill-current" />
                      <span className="text-xs text-muted-foreground">Popular</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}