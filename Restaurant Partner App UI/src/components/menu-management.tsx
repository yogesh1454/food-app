import React, { useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from './ui/card';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';
import { Textarea } from './ui/textarea';
import { Badge } from './ui/badge';
import { Switch } from './ui/switch';
import { Tabs, TabsContent, TabsList, TabsTrigger } from './ui/tabs';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/select';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from './ui/dialog';
import { 
  Plus, 
  Search, 
  Filter, 
  Edit, 
  Eye,
  MoreVertical,
  Camera,
  Upload,
  Star,
  Clock,
  IndianRupee,
  Package,
  Utensils,
  ChefHat,
  Leaf,
  Flame,
  Award,
  TrendingUp,
  Sparkles,
  Info,
  Zap,
  Target,
  Heart,
  Activity
} from 'lucide-react';
import { ImageWithFallback } from './figma/ImageWithFallback';

export function MenuManagement() {
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [isAddItemOpen, setIsAddItemOpen] = useState(false);

  const categories = [
    { id: 'all', name: 'All Items', count: 23 },
    { id: 'starters', name: 'Starters', count: 8 },
    { id: 'mains', name: 'Main Course', count: 10 },
    { id: 'beverages', name: 'Beverages', count: 5 }
  ];

  const menuItems = [
    {
      id: 1,
      name: 'Paneer Tikka Roll',
      description: 'Tender paneer tikka wrapped in soft naan with mint chutney',
      price: 180,
      category: 'mains',
      image: '/api/placeholder/120/120',
      isVeg: true,
      isAvailable: true,
      popularity: 'high',
      spiceLevel: 'medium',
      prepTime: 15,
      variants: [
        { name: 'Regular', price: 180 },
        { name: 'Large', price: 220 }
      ],
      customizations: ['Extra Cheese', 'Less Spicy', 'No Onions'],
      aiDescription: {
        nutrition: {
          calories: 380,
          protein: 18,
          carbs: 42,
          fat: 14,
          fiber: 4
        },
        healthInsights: [
          'High in plant-based protein',
          'Good source of calcium',
          'Contains healthy carbohydrates'
        ],
        dietaryTags: ['Vegetarian', 'High Protein', 'Contains Dairy'],
        seoDescription: 'Delicious Paneer Tikka Roll - A protein-rich vegetarian delight with tender cottage cheese, aromatic spices, and fresh mint chutney. Perfect for health-conscious food lovers seeking authentic Indian flavors with 18g protein per serving.'
      }
    },
    {
      id: 2,
      name: 'Chicken Biryani',
      description: 'Aromatic basmati rice cooked with tender chicken and spices',
      price: 220,
      category: 'mains',
      image: '/api/placeholder/120/120',
      isVeg: false,
      isAvailable: true,
      popularity: 'high',
      spiceLevel: 'high',
      prepTime: 25,
      variants: [
        { name: 'Regular', price: 220 },
        { name: 'Large', price: 280 }
      ],
      customizations: ['Extra Raita', 'Less Spicy', 'Extra Gravy'],
      aiDescription: {
        nutrition: {
          calories: 520,
          protein: 28,
          carbs: 65,
          fat: 16,
          fiber: 2
        },
        healthInsights: [
          'Excellent source of lean protein',
          'Rich in B vitamins',
          'Provides essential amino acids'
        ],
        dietaryTags: ['High Protein', 'Contains Gluten', 'Halal'],
        seoDescription: 'Authentic Chicken Biryani - Traditional aromatic basmati rice layered with succulent chicken pieces and exotic spices. A complete meal with 28g protein, perfect for biryani lovers seeking genuine Hyderabadi flavors.'
      }
    },
    {
      id: 3,
      name: 'Masala Dosa',
      description: 'Crispy fermented crepe filled with spiced potato filling',
      price: 120,
      category: 'mains',
      image: '/api/placeholder/120/120',
      isVeg: true,
      isAvailable: false,
      popularity: 'medium',
      spiceLevel: 'medium',
      prepTime: 12,
      variants: [
        { name: 'Plain', price: 100 },
        { name: 'Masala', price: 120 }
      ],
      customizations: ['Extra Sambar', 'Extra Chutney'],
      aiDescription: {
        nutrition: {
          calories: 290,
          protein: 8,
          carbs: 48,
          fat: 8,
          fiber: 6
        },
        healthInsights: [
          'Fermented food aids digestion',
          'Low in fat',
          'Good source of complex carbs',
          'Probiotic benefits'
        ],
        dietaryTags: ['Vegetarian', 'Gluten-Free', 'Fermented', 'South Indian'],
        seoDescription: 'Crispy Masala Dosa - Traditional South Indian fermented crepe with spiced potato filling. A healthy, gluten-free option with probiotics and fiber. Served with coconut chutney and sambar for authentic Chennai taste.'
      }
    },
    {
      id: 4,
      name: 'Samosa Chat',
      description: 'Crispy samosas topped with yogurt, chutneys and spices',
      price: 80,
      category: 'starters',
      image: '/api/placeholder/120/120',
      isVeg: true,
      isAvailable: true,
      popularity: 'medium',
      spiceLevel: 'medium',
      prepTime: 8,
      variants: [],
      customizations: ['Extra Yogurt', 'Less Spicy'],
      aiDescription: {
        nutrition: {
          calories: 220,
          protein: 6,
          carbs: 28,
          fat: 10,
          fiber: 3
        },
        healthInsights: [
          'Contains probiotics from yogurt',
          'Rich in vitamin C from chutneys',
          'Provides dietary fiber'
        ],
        dietaryTags: ['Vegetarian', 'Street Food', 'Contains Dairy'],
        seoDescription: 'Delicious Samosa Chat - Crispy samosas topped with cooling yogurt, tangy chutneys, and aromatic spices. A popular Indian street food snack that combines textures and flavors for the perfect evening treat.'
      }
    }
  ];

  const filteredItems = menuItems.filter(item => {
    const matchesCategory = selectedCategory === 'all' || item.category === selectedCategory;
    const matchesSearch = item.name.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  const getPopularityColor = (popularity: string) => {
    switch (popularity) {
      case 'high': return 'bg-green-100 text-green-800';
      case 'medium': return 'bg-yellow-100 text-yellow-800';
      case 'low': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getSpiceLevelIcon = (level: string) => {
    switch (level) {
      case 'low': return <Flame className="w-3 h-3 text-green-600" />;
      case 'medium': return <Flame className="w-3 h-3 text-yellow-600" />;
      case 'high': return <Flame className="w-3 h-3 text-red-600" />;
      default: return null;
    }
  };

  return (
    <div className="p-6 space-y-6 max-h-screen overflow-y-auto">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Menu Management</h1>
          <p className="text-muted-foreground">Manage your restaurant's menu items</p>
        </div>
        <Dialog open={isAddItemOpen} onOpenChange={setIsAddItemOpen}>
          <DialogTrigger asChild>
            <Button className="gap-2 bg-[#16a34a] hover:bg-[#15803d]">
              <Plus className="w-4 h-4" />
              Add New Item
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Add New Menu Item</DialogTitle>
              <DialogDescription>Create a new item for your menu</DialogDescription>
            </DialogHeader>
            <AddItemForm onClose={() => setIsAddItemOpen(false)} />
          </DialogContent>
        </Dialog>
      </div>

      {/* Filters and Search */}
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="flex-1">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <Input
              placeholder="Search menu items..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10"
            />
          </div>
        </div>
        <div className="flex gap-2 overflow-x-auto">
          {categories.map((category) => (
            <Button
              key={category.id}
              variant={selectedCategory === category.id ? 'default' : 'outline'}
              size="sm"
              onClick={() => setSelectedCategory(category.id)}
              className="whitespace-nowrap"
            >
              {category.name} ({category.count})
            </Button>
          ))}
        </div>
      </div>

      {/* Menu Items Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredItems.map((item) => (
          <Card key={item.id} className="overflow-hidden hover:shadow-lg transition-shadow">
            <div className="relative">
              <div className="w-full h-48 bg-muted relative">
                <ImageWithFallback
                  src="/api/placeholder/300/200"
                  alt={item.name}
                  className="w-full h-full object-cover"
                />
                <div className="absolute top-3 left-3 flex gap-2">
                  {item.isVeg ? (
                    <Badge className="bg-green-100 text-green-800">
                      <Leaf className="w-3 h-3 mr-1" />
                      Veg
                    </Badge>
                  ) : (
                    <Badge className="bg-red-100 text-red-800">Non-Veg</Badge>
                  )}
                  <Badge className={getPopularityColor(item.popularity)}>
                    {item.popularity === 'high' && <TrendingUp className="w-3 h-3 mr-1" />}
                    {item.popularity}
                  </Badge>
                </div>
                <div className="absolute top-3 right-3">
                  <Switch
                    checked={item.isAvailable}
                    className="data-[state=checked]:bg-green-600"
                  />
                </div>
              </div>
            </div>

            <CardContent className="p-4">
              <div className="space-y-3">
                <div>
                  <div className="flex items-center justify-between">
                    <h3 className="font-semibold">{item.name}</h3>
                    <div className="flex items-center gap-1">
                      {getSpiceLevelIcon(item.spiceLevel)}
                      <span className="text-xs text-muted-foreground">{item.spiceLevel}</span>
                    </div>
                  </div>
                  <p className="text-sm text-muted-foreground line-clamp-2">{item.description}</p>
                </div>

                <div className="flex items-center gap-4">
                  <div className="flex items-center gap-1">
                    <IndianRupee className="w-4 h-4 text-[#16a34a]" />
                    <span className="font-semibold">₹{item.price}</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <Clock className="w-4 h-4 text-muted-foreground" />
                    <span className="text-sm text-muted-foreground">{item.prepTime}m</span>
                  </div>
                </div>

                {item.variants.length > 0 && (
                  <div>
                    <p className="text-xs text-muted-foreground mb-1">Variants:</p>
                    <div className="flex gap-1 flex-wrap">
                      {item.variants.map((variant, index) => (
                        <Badge key={index} variant="outline" className="text-xs">
                          {variant.name} (₹{variant.price})
                        </Badge>
                      ))}
                    </div>
                  </div>
                )}

                {/* AI Nutrition Info */}
                {item.aiDescription && (
                  <div className="bg-[#f0fdf4] p-3 rounded-lg border border-[#16a34a]/20">
                    <div className="flex items-center gap-2 mb-2">
                      <Sparkles className="w-3 h-3 text-[#16a34a]" />
                      <span className="text-xs font-medium text-[#16a34a]">AI Nutrition Insights</span>
                    </div>
                    <div className="grid grid-cols-3 gap-2 mb-2">
                      <div className="text-center">
                        <p className="text-xs font-medium">{item.aiDescription.nutrition.calories}</p>
                        <p className="text-[10px] text-muted-foreground">Calories</p>
                      </div>
                      <div className="text-center">
                        <p className="text-xs font-medium">{item.aiDescription.nutrition.protein}g</p>
                        <p className="text-[10px] text-muted-foreground">Protein</p>
                      </div>
                      <div className="text-center">
                        <p className="text-xs font-medium">{item.aiDescription.nutrition.fiber}g</p>
                        <p className="text-[10px] text-muted-foreground">Fiber</p>
                      </div>
                    </div>
                    <div className="flex gap-1 flex-wrap">
                      {item.aiDescription.healthInsights.slice(0, 2).map((insight, index) => (
                        <Badge key={index} variant="outline" className="text-[10px] border-[#16a34a]/30 text-[#16a34a]">
                          <Heart className="w-2 h-2 mr-1" />
                          {insight}
                        </Badge>
                      ))}
                    </div>
                  </div>
                )}

                {item.customizations.length > 0 && (
                  <div>
                    <p className="text-xs text-muted-foreground mb-1">Customizations:</p>
                    <div className="flex gap-1 flex-wrap">
                      {item.customizations.slice(0, 2).map((custom, index) => (
                        <Badge key={index} variant="secondary" className="text-xs">
                          {custom}
                        </Badge>
                      ))}
                      {item.customizations.length > 2 && (
                        <Badge variant="secondary" className="text-xs">
                          +{item.customizations.length - 2} more
                        </Badge>
                      )}
                    </div>
                  </div>
                )}

                <div className="flex gap-2 pt-2">
                  <Button size="sm" variant="outline" className="flex-1 gap-2">
                    <Edit className="w-3 h-3" />
                    Edit
                  </Button>
                  <Button size="sm" variant="outline" className="flex-1 gap-2">
                    <Eye className="w-3 h-3" />
                    View
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      {filteredItems.length === 0 && (
        <div className="text-center py-12">
          <Package className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
          <h3 className="font-semibold mb-2">No items found</h3>
          <p className="text-muted-foreground mb-4">Try adjusting your search or filters</p>
          <Button onClick={() => setIsAddItemOpen(true)} className="bg-[#16a34a] hover:bg-[#15803d]">
            Add Your First Item
          </Button>
        </div>
      )}
    </div>
  );
}

function AddItemForm({ onClose }: { onClose: () => void }) {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    category: '',
    prepTime: '',
    spiceLevel: 'medium',
    isVeg: true
  });

  return (
    <div className="space-y-6">
      <Tabs defaultValue="basic" className="w-full">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="basic">Basic Info</TabsTrigger>
          <TabsTrigger value="details">Details</TabsTrigger>
          <TabsTrigger value="nutrition">AI Nutrition</TabsTrigger>
          <TabsTrigger value="images">Images</TabsTrigger>
        </TabsList>

        <TabsContent value="basic" className="space-y-4">
          <div>
            <Label htmlFor="name">Item Name</Label>
            <Input
              id="name"
              placeholder="e.g., Paneer Tikka Roll"
              value={formData.name}
              onChange={(e) => setFormData({...formData, name: e.target.value})}
            />
          </div>

          <div>
            <Label htmlFor="description">Description</Label>
            <Textarea
              id="description"
              placeholder="Describe your dish..."
              value={formData.description}
              onChange={(e) => setFormData({...formData, description: e.target.value})}
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="price">Price (₹)</Label>
              <Input
                id="price"
                type="number"
                placeholder="180"
                value={formData.price}
                onChange={(e) => setFormData({...formData, price: e.target.value})}
              />
            </div>
            <div>
              <Label htmlFor="category">Category</Label>
              <Select value={formData.category} onValueChange={(value) => setFormData({...formData, category: value})}>
                <SelectTrigger>
                  <SelectValue placeholder="Select category" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="starters">Starters</SelectItem>
                  <SelectItem value="mains">Main Course</SelectItem>
                  <SelectItem value="beverages">Beverages</SelectItem>
                  <SelectItem value="desserts">Desserts</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </TabsContent>

        <TabsContent value="details" className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="prepTime">Prep Time (minutes)</Label>
              <Input
                id="prepTime"
                type="number"
                placeholder="15"
                value={formData.prepTime}
                onChange={(e) => setFormData({...formData, prepTime: e.target.value})}
              />
            </div>
            <div>
              <Label htmlFor="spiceLevel">Spice Level</Label>
              <Select value={formData.spiceLevel} onValueChange={(value) => setFormData({...formData, spiceLevel: value})}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="low">Low</SelectItem>
                  <SelectItem value="medium">Medium</SelectItem>
                  <SelectItem value="high">High</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          <div className="flex items-center justify-between">
            <Label htmlFor="isVeg">Vegetarian Item</Label>
            <Switch
              id="isVeg"
              checked={formData.isVeg}
              onCheckedChange={(checked) => setFormData({...formData, isVeg: checked})}
            />
          </div>
        </TabsContent>

        <TabsContent value="nutrition" className="space-y-4">
          <div className="bg-gradient-to-r from-[#f0fdf4] to-white p-6 rounded-xl border border-[#16a34a]/20">
            <div className="flex items-center gap-3 mb-4">
              <Sparkles className="w-6 h-6 text-[#16a34a]" />
              <div>
                <h3 className="font-semibold">AI-Generated Nutrition & SEO Content</h3>
                <p className="text-sm text-muted-foreground">Smart insights automatically generated for your dish</p>
              </div>
            </div>
            
            <div className="space-y-4">
              <div>
                <Label>Nutritional Information</Label>
                <div className="grid grid-cols-2 gap-4 mt-2">
                  <div>
                    <Label className="text-xs">Calories</Label>
                    <Input placeholder="Auto-generated: 380" disabled className="bg-muted/50" />
                  </div>
                  <div>
                    <Label className="text-xs">Protein (g)</Label>
                    <Input placeholder="Auto-generated: 18g" disabled className="bg-muted/50" />
                  </div>
                  <div>
                    <Label className="text-xs">Carbs (g)</Label>
                    <Input placeholder="Auto-generated: 42g" disabled className="bg-muted/50" />
                  </div>
                  <div>
                    <Label className="text-xs">Fiber (g)</Label>
                    <Input placeholder="Auto-generated: 4g" disabled className="bg-muted/50" />
                  </div>
                </div>
              </div>

              <div>
                <Label>Health Insights (Editable)</Label>
                <Textarea 
                  placeholder="AI will generate health benefits like: High in plant-based protein, Good source of calcium, Contains healthy carbohydrates..."
                  className="mt-2"
                  rows={3}
                />
              </div>

              <div>
                <Label>SEO Description (Editable)</Label>
                <Textarea 
                  placeholder="AI will generate SEO-optimized description with keywords, nutrition highlights, and appealing copy for search engines..."
                  className="mt-2"
                  rows={4}
                />
              </div>

              <div>
                <Label>Dietary Tags</Label>
                <div className="flex gap-2 flex-wrap mt-2">
                  <Badge variant="outline" className="text-xs">Vegetarian</Badge>
                  <Badge variant="outline" className="text-xs">High Protein</Badge>
                  <Badge variant="outline" className="text-xs">Contains Dairy</Badge>
                  <Button size="sm" variant="ghost" className="text-xs h-6">
                    <Plus className="w-3 h-3 mr-1" />
                    Add Tag
                  </Button>
                </div>
              </div>

              <div className="bg-blue-50 p-4 rounded-lg border border-blue-200">
                <div className="flex items-start gap-3">
                  <Info className="w-5 h-5 text-blue-600 mt-0.5" />
                  <div>
                    <h4 className="font-medium text-blue-900">AI Generation Process</h4>
                    <p className="text-sm text-blue-700 mt-1">
                      Our AI analyzes your dish name, ingredients, and cooking method to generate accurate nutrition data, 
                      health insights, and SEO-optimized descriptions. You can edit all generated content to match your preferences.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </TabsContent>

        <TabsContent value="images" className="space-y-4">
          <div className="border-2 border-dashed border-border rounded-xl p-8 text-center hover:border-[#16a34a] transition-colors cursor-pointer">
            <Camera className="w-8 h-8 text-muted-foreground mx-auto mb-2" />
            <p className="font-medium">Upload Item Photos</p>
            <p className="text-sm text-muted-foreground">Add multiple high-quality images</p>
          </div>
        </TabsContent>
      </Tabs>

      <div className="flex gap-3 pt-4 border-t">
        <Button variant="outline" onClick={onClose} className="flex-1">
          Cancel
        </Button>
        <Button className="flex-1 bg-[#16a34a] hover:bg-[#15803d]">
          <Sparkles className="w-4 h-4 mr-2" />
          Generate AI Content & Add Item
        </Button>
      </div>
    </div>
  );
}