import { useEffect, useState } from "react";
import { PortalTopbar } from "../../comoponents/PortalTopbar";
import { Card, CardContent, CardHeader, CardTitle } from "../../comoponents/ui/card";
import { Button } from "../../comoponents/ui/button";
import { Input } from "../../comoponents/ui/input";
import { Label } from "../../comoponents/ui/label";
import { Switch } from "../../comoponents/ui/switch";
import { Separator } from "../../comoponents/ui/separator";
import { Textarea } from "../../comoponents/ui/textarea";
import apiService from "../../services/api";
import { toast } from "sonner";

export function JobSeekerSettings() {
  const [profile, setProfile] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let mounted = true;
    (async () => {
      try {
        const res = await apiService.getUserProfile();
        if (!mounted) return;
        setProfile({
          firstName: res.name?.split(" ")?.[0] || "",
          lastName: res.name?.split(" ")?.slice(1).join(" ") || "",
          email: res.email || "",
          phone: res.location || "",
          location: res.location || "",
          bio: res.location || "",
        });
      } catch (err) {
        console.warn("Failed to load profile", err);
      }
    })();
    return () => { mounted = false; };
  }, []);

  const handleSave = async () => {
    if (!profile) return;
    setLoading(true);
    try {
      const payload = {
        name: `${profile.firstName} ${profile.lastName}`.trim(),
        email: profile.email,
        location: profile.location,
        bio: profile.bio,
        phone: profile.phone,
      };
      await apiService.updateUserProfile(payload);
      toast.success("Profile updated");
    } catch (err: any) {
      console.error("Profile update failed", err);
      toast.error(err?.message || "Failed to update profile");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <PortalTopbar title="Settings" subtitle="Manage your account and preferences" />
      <main className="flex-1 overflow-auto p-6">
        <div className="max-w-4xl space-y-6">
          {/* Profile Settings */}
          <Card>
            <CardHeader>
              <CardTitle>Profile Information</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="firstName">First Name</Label>
                  <Input id="firstName" value={profile?.firstName || ""} onChange={(e) => setProfile({...profile, firstName: e.target.value})} />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="lastName">Last Name</Label>
                  <Input id="lastName" value={profile?.lastName || ""} onChange={(e) => setProfile({...profile, lastName: e.target.value})} />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input id="email" type="email" value={profile?.email || ""} onChange={(e) => setProfile({...profile, email: e.target.value})} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="phone">Phone</Label>
                <Input id="phone" type="tel" value={profile?.phone || ""} onChange={(e) => setProfile({...profile, phone: e.target.value})} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="location">Location</Label>
                <Input id="location" value={profile?.location || ""} onChange={(e) => setProfile({...profile, location: e.target.value})} />
              </div>
              <div className="space-y-2">
                <Label htmlFor="bio">Bio</Label>
                <Textarea 
                  id="bio" 
                  value={profile?.bio || ""}
                  onChange={(e) => setProfile({...profile, bio: e.target.value})}
                  rows={4}
                />
              </div>
            </CardContent>
          </Card>

          {/* Job Preferences */}
          <Card>
            <CardHeader>
              <CardTitle>Job Preferences</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="space-y-2">
                <Label htmlFor="jobTitle">Desired Job Title</Label>
                <Input id="jobTitle" defaultValue="Senior React Developer" />
              </div>
              <div className="space-y-2">
                <Label htmlFor="salary">Expected Salary Range</Label>
                <Input id="salary" defaultValue="$120,000 - $150,000" />
              </div>
              <div className="space-y-2">
                <Label htmlFor="workType">Work Type</Label>
                <Input id="workType" defaultValue="Remote, Hybrid" />
              </div>
            </CardContent>
          </Card>

          {/* Notification Settings */}
          <Card>
            <CardHeader>
              <CardTitle>Notifications</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <Label>Email Notifications</Label>
                  <p className="text-sm text-gray-500">Receive updates about your applications</p>
                </div>
                <Switch defaultChecked />
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <div>
                  <Label>Job Recommendations</Label>
                  <p className="text-sm text-gray-500">Get AI-powered job suggestions</p>
                </div>
                <Switch defaultChecked />
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <div>
                  <Label>Application Reminders</Label>
                  <p className="text-sm text-gray-500">Reminders for follow-ups</p>
                </div>
                <Switch defaultChecked />
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <div>
                  <Label>Profile Views</Label>
                  <p className="text-sm text-gray-500">Get notified when employers view your profile</p>
                </div>
                <Switch defaultChecked />
              </div>
            </CardContent>
          </Card>

          {/* Privacy Settings */}
          <Card>
            <CardHeader>
              <CardTitle>Privacy</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <Label>Public Profile</Label>
                  <p className="text-sm text-gray-500">Make your profile visible to employers</p>
                </div>
                <Switch defaultChecked />
              </div>
              <Separator />
              <div className="flex items-center justify-between">
                <div>
                  <Label>AI Data Usage</Label>
                  <p className="text-sm text-gray-500">Allow AI to analyze your data for better recommendations</p>
                </div>
                <Switch defaultChecked />
              </div>
            </CardContent>
          </Card>

          {/* Actions */}
          <div className="flex justify-end gap-4">
            <Button variant="outline" onClick={() => window.location.reload()}>Cancel</Button>
            <Button className="bg-green-500 hover:bg-green-600" onClick={handleSave} disabled={loading}>{loading ? 'Saving...' : 'Save Changes'}</Button>
          </div>
        </div>
      </main>
    </>
  );
}
