import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Badge } from './ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { Avatar, AvatarFallback, AvatarImage } from './ui/avatar';
import { Separator } from './ui/separator';
import { 
  Clock, 
  CheckCircle, 
  Package, 
  Truck,
  Phone,
  MessageCircle,
  MapPin,
  Timer,
  IndianRupee,
  Star,
  AlertCircle,
  ChefHat,
  Utensils,
  Calendar,
  Filter,
  Search,
  MoreVertical,
  Play,
  Pause,
  RotateCcw
} from 'lucide-react';

export function OrderManagement() {
  const [selectedTab, setSelectedTab] = useState('new');

  const orders = [
    {
      id: '#1238',
      customer: {
        name: 'Arjun Sharma',
        phone: '+91 98765 43210',
        avatar: '/api/placeholder/40/40'
      },
      items: [
        { name: 'Paneer Tikka Roll', quantity: 2, price: 360, customizations: ['Extra cheese', 'Medium spicy'] },
        { name: 'Masala Chai', quantity: 1, price: 40, customizations: [] }
      ],
      total: 400,
      status: 'new',
      orderTime: '2 min ago',
      deliveryTime: '25-30 min',
      address: '123, MG Road, Sector 14, Gurgaon',
      specialInstructions: 'Please ring the bell twice',
      priority: 'normal',
      paymentStatus: 'paid'
    },
    {
      id: '#1237',
      customer: {
        name: 'Sneha Reddy',
        phone: '+91 87654 32109',
        avatar: '/api/placeholder/40/40'
      },
      items: [
        { name: 'Chicken Biryani', quantity: 1, price: 220, customizations: ['Extra raita'] },
        { name: 'Gulab Jamun', quantity: 2, price: 120, customizations: [] }
      ],
      total: 340,
      status: 'preparing',
      orderTime: '8 min ago',
      deliveryTime: '20-25 min',
      address: '456, Park Street, DLF Phase 2',
      specialInstructions: 'Call before delivery',
      priority: 'high',
      paymentStatus: 'paid'
    },
    {
      id: '#1236',
      customer: {
        name: 'Vikram Singh',
        phone: '+91 76543 21098',
        avatar: '/api/placeholder/40/40'
      },
      items: [
        { name: 'Masala Dosa', quantity: 2, price: 240, customizations: [] },
        { name: 'Filter Coffee', quantity: 2, price: 80, customizations: [] }
      ],
      total: 320,
      status: 'ready',
      orderTime: '15 min ago',
      deliveryTime: '10-15 min',
      address: '789, Golf Course Road, DLF Phase 3',
      specialInstructions: '',
      priority: 'normal',
      paymentStatus: 'paid'
    },
    {
      id: '#1235',
      customer: {
        name: 'Priya Malhotra',
        phone: '+91 65432 10987',
        avatar: '/api/placeholder/40/40'
      },
      items: [
        { name: 'Veg Thali', quantity: 1, price: 180, customizations: ['Less spicy'] }
      ],
      total: 180,
      status: 'delivered',
      orderTime: '45 min ago',
      deliveryTime: 'Delivered',
      address: '321, Central Park, Sector 42',
      specialInstructions: '',
      priority: 'normal',
      paymentStatus: 'paid'
    }
  ];

  const getStatusConfig = (status: string) => {
    switch (status) {
      case 'new':
        return {
          color: 'bg-blue-100 text-blue-800',
          icon: AlertCircle,
          nextAction: 'Accept Order',
          nextStatus: 'preparing'
        };
      case 'preparing':
        return {
          color: 'bg-orange-100 text-orange-800',
          icon: ChefHat,
          nextAction: 'Mark Ready',
          nextStatus: 'ready'
        };
      case 'ready':
        return {
          color: 'bg-green-100 text-green-800',
          icon: Package,
          nextAction: 'Out for Delivery',
          nextStatus: 'out-for-delivery'
        };
      case 'out-for-delivery':
        return {
          color: 'bg-purple-100 text-purple-800',
          icon: Truck,
          nextAction: 'Mark Delivered',
          nextStatus: 'delivered'
        };
      case 'delivered':
        return {
          color: 'bg-gray-100 text-gray-800',
          icon: CheckCircle,
          nextAction: null,
          nextStatus: null
        };
      default:
        return {
          color: 'bg-gray-100 text-gray-800',
          icon: AlertCircle,
          nextAction: null,
          nextStatus: null
        };
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'high': return 'border-l-red-500 bg-red-50';
      case 'normal': return 'border-l-gray-300 bg-white';
      default: return 'border-l-gray-300 bg-white';
    }
  };

  const filteredOrders = selectedTab === 'all' 
    ? orders 
    : orders.filter(order => order.status === selectedTab || 
        (selectedTab === 'out-for-delivery' && order.status === 'out-for-delivery'));

  const orderCounts = {
    new: orders.filter(o => o.status === 'new').length,
    preparing: orders.filter(o => o.status === 'preparing').length,
    ready: orders.filter(o => o.status === 'ready').length,
    'out-for-delivery': orders.filter(o => o.status === 'out-for-delivery').length,
    delivered: orders.filter(o => o.status === 'delivered').length
  };

  return (
    <div className="p-6 space-y-6 max-h-screen overflow-y-auto">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Order Management</h1>
          <p className="text-muted-foreground">Track and manage incoming orders</p>
        </div>
        <div className="flex gap-3">
          <Button variant="outline" className="gap-2">
            <Filter className="w-4 h-4" />
            Filter
          </Button>
          <Button variant="outline" className="gap-2">
            <Calendar className="w-4 h-4" />
            Today
          </Button>
        </div>
      </div>

      {/* Order Status Tabs */}
      <Tabs value={selectedTab} onValueChange={setSelectedTab}>
        <TabsList className="grid w-full grid-cols-5">
          <TabsTrigger value="new" className="relative">
            New Orders
            {orderCounts.new > 0 && (
              <Badge className="ml-2 h-5 w-5 p-0 text-xs bg-red-500 text-white">
                {orderCounts.new}
              </Badge>
            )}
          </TabsTrigger>
          <TabsTrigger value="preparing" className="relative">
            Preparing
            {orderCounts.preparing > 0 && (
              <Badge className="ml-2 h-5 w-5 p-0 text-xs bg-orange-500 text-white">
                {orderCounts.preparing}
              </Badge>
            )}
          </TabsTrigger>
          <TabsTrigger value="ready" className="relative">
            Ready
            {orderCounts.ready > 0 && (
              <Badge className="ml-2 h-5 w-5 p-0 text-xs bg-green-500 text-white">
                {orderCounts.ready}
              </Badge>
            )}
          </TabsTrigger>
          <TabsTrigger value="out-for-delivery">
            Out for Delivery
          </TabsTrigger>
          <TabsTrigger value="delivered">
            Delivered
          </TabsTrigger>
        </TabsList>

        <div className="mt-6">
          {/* Orders List */}
          <div className="space-y-4">
            {filteredOrders.map((order) => {
              const statusConfig = getStatusConfig(order.status);
              const StatusIcon = statusConfig.icon;

              return (
                <Card key={order.id} className={`border-l-4 ${getPriorityColor(order.priority)} hover:shadow-lg transition-shadow`}>
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex items-center gap-4">
                        <Avatar className="w-12 h-12">
                          <AvatarImage src={order.customer.avatar} />
                          <AvatarFallback>{order.customer.name.split(' ').map(n => n[0]).join('')}</AvatarFallback>
                        </Avatar>
                        <div>
                          <div className="flex items-center gap-3">
                            <h3 className="font-semibold text-lg">{order.id}</h3>
                            <Badge className={statusConfig.color}>
                              <StatusIcon className="w-3 h-3 mr-1" />
                              {order.status.replace('-', ' ')}
                            </Badge>
                            {order.priority === 'high' && (
                              <Badge className="bg-red-100 text-red-800">
                                Priority
                              </Badge>
                            )}
                          </div>
                          <p className="text-muted-foreground">{order.customer.name}</p>
                          <div className="flex items-center gap-4 text-sm text-muted-foreground">
                            <span className="flex items-center gap-1">
                              <Timer className="w-3 h-3" />
                              {order.orderTime}
                            </span>
                            <span className="flex items-center gap-1">
                              <Clock className="w-3 h-3" />
                              {order.deliveryTime}
                            </span>
                          </div>
                        </div>
                      </div>
                      
                      <div className="text-right">
                        <p className="text-2xl font-bold text-[#16a34a]">₹{order.total}</p>
                        <p className="text-sm text-muted-foreground">{order.items.length} items</p>
                      </div>
                    </div>

                    <Separator className="my-4" />

                    {/* Order Items */}
                    <div className="space-y-2 mb-4">
                      {order.items.map((item, index) => (
                        <div key={index} className="flex items-center justify-between">
                          <div className="flex items-center gap-3">
                            <div className="w-2 h-2 bg-[#16a34a] rounded-full"></div>
                            <div>
                              <span className="font-medium">{item.quantity}x {item.name}</span>
                              {item.customizations.length > 0 && (
                                <div className="text-xs text-muted-foreground">
                                  {item.customizations.join(', ')}
                                </div>
                              )}
                            </div>
                          </div>
                          <span className="font-medium">₹{item.price}</span>
                        </div>
                      ))}
                    </div>

                    {order.specialInstructions && (
                      <div className="bg-yellow-50 p-3 rounded-lg border border-yellow-200 mb-4">
                        <p className="text-sm">
                          <strong>Special Instructions:</strong> {order.specialInstructions}
                        </p>
                      </div>
                    )}

                    <div className="flex items-center justify-between text-sm text-muted-foreground mb-4">
                      <div className="flex items-center gap-1">
                        <MapPin className="w-3 h-3" />
                        <span className="truncate max-w-xs">{order.address}</span>
                      </div>
                      <div className="flex items-center gap-1">
                        <Phone className="w-3 h-3" />
                        <span>{order.customer.phone}</span>
                      </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="flex gap-3">
                      {statusConfig.nextAction && (
                        <Button className="flex-1 bg-[#16a34a] hover:bg-[#15803d]">
                          {statusConfig.nextAction}
                        </Button>
                      )}
                      <Button variant="outline" size="icon">
                        <Phone className="w-4 h-4" />
                      </Button>
                      <Button variant="outline" size="icon">
                        <MessageCircle className="w-4 h-4" />
                      </Button>
                      <Button variant="outline" size="icon">
                        <MoreVertical className="w-4 h-4" />
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              );
            })}
          </div>

          {filteredOrders.length === 0 && (
            <div className="text-center py-12">
              <Package className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
              <h3 className="font-semibold mb-2">No orders found</h3>
              <p className="text-muted-foreground">
                {selectedTab === 'new' ? 'No new orders at the moment' : `No ${selectedTab.replace('-', ' ')} orders`}
              </p>
            </div>
          )}
        </div>
      </Tabs>
    </div>
  );
}