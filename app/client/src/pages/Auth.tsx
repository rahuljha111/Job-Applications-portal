import { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router";
import { Briefcase, ArrowLeft, Mail, Lock, User, Eye, EyeOff, ChevronDown } from "lucide-react";
import { Button } from "../comoponents/ui/button";
import { Input } from "../comoponents/ui/input";
import { Label } from "../comoponents/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../comoponents/ui/tabs";
import { cn } from "../lib/utils";
import { useAuth } from "../contexts/AuthContext";
import { toast } from "sonner";

export function Auth() {
  const [activeTab, setActiveTab] = useState("login");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [role, setRole] = useState("CANDIDATE");
  const [passwordErrors, setPasswordErrors] = useState<string[]>([]);
  
  const { login, register, error, clearError } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 40);
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  // Clear errors when switching tabs
  useEffect(() => {
    clearError();
  }, [activeTab, clearError]);

  // Form States
  const [loginData, setLoginData] = useState({ email: "", password: "" });
  const [signupData, setSignupData] = useState({ 
    firstName: "", 
    lastName: "", 
    email: "", 
    password: "", 
    confirmPassword: "" 
  });
  const [forgotEmail, setForgotEmail] = useState("");
  const [resetSent, setResetSent] = useState(false);

  const handleLoginSubmit = async (e: React.FormEvent) => {
    e.preventDefault(); 
    if (!loginData.email || !loginData.password) return;
    
    setIsLoading(true);
    try {
      await login(loginData.email, loginData.password);
      toast.success("Login successful! Welcome back.");
      navigate('/dashboard'); // Navigate to dashboard after successful login
    } catch (err) {
      toast.error("Login failed. Please check your credentials.");
    } finally {
      setIsLoading(false);
    }
  };

  const checkPasswordRequirements = (password: string): string[] => {
    const errors: string[] = [];
    if (password.length < 8) errors.push("At least 8 characters");
    if (!/[A-Z]/.test(password)) errors.push("One uppercase letter");
    if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) errors.push("One special character");
    return errors;
  };

  const validatePassword = (password: string): { isValid: boolean; message: string } => {
    if (password.length < 8) {
      return { isValid: false, message: "Password must be at least 8 characters long" };
    }
    if (!/[A-Z]/.test(password)) {
      return { isValid: false, message: "Password must contain at least one uppercase letter" };
    }
    if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
      return { isValid: false, message: "Password must contain at least one special character (!@#$%^&* etc.)" };
    }
    return { isValid: true, message: "" };
  };

  const handleSignupSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!signupData.firstName || !signupData.lastName || !signupData.email || !signupData.password) {
      toast.error("Please fill in all fields");
      return;
    }

    const passwordValidation = validatePassword(signupData.password);
    if (!passwordValidation.isValid) {
      toast.error(passwordValidation.message);
      return;
    }

    if (signupData.password !== signupData.confirmPassword) {
      toast.error("Passwords do not match");
      return;
    }

    setIsLoading(true);
    try {
      const response = await register({
        firstName: signupData.firstName,
        lastName: signupData.lastName,
        email: signupData.email,
        password: signupData.password,
        role: role
      });
      
      console.log('Registration successful, response:', response);
      toast.success("Registration successful! Please check your email to verify your account.");
      
      // Store email for verification page
      const verificationUrl = `/email-verification?email=${encodeURIComponent(signupData.email)}`;
      console.log('Navigating to:', verificationUrl);
      
      // Redirect to email verification page
      navigate(verificationUrl);
    } catch (err) {
      console.error('Registration error:', err);
      toast.error("Registration failed. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#fafafa] flex flex-col font-sans text-slate-900">
      
      {/* NAVBAR */}
      <nav className={cn(
        "fixed top-0 w-full z-50 transition-all duration-500",
        scrolled 
          ? "bg-white/90 backdrop-blur-xl py-3 border-b border-slate-200 shadow-sm" 
          : "bg-transparent py-6 border-b border-transparent"
      )}>
        <div className="max-w-7xl mx-auto px-6">
          <div className="flex h-12 items-center justify-between">
            {/* Logo */}
            <Link to="/" className="flex items-center gap-2 group">
              <div className="bg-slate-950 p-2 rounded-xl group-hover:bg-indigo-600 transition-all duration-300 shadow-lg">
                <Briefcase className="h-5 w-5 text-white" />
              </div>
              <span className="text-2xl font-black tracking-tighter text-slate-950 hidden sm:block">
                JobTracker<span className="text-indigo-600">.</span>
              </span>
            </Link>

            {/* Center Links */}
            <div className="hidden lg:flex items-center gap-1 bg-slate-100/50 p-1 rounded-full backdrop-blur-sm border border-slate-200/50">
              {["Find Jobs", "Companies", "About"].map((item) => (
                <Link 
                  key={item} 
                  to="#" 
                  className="px-5 py-1.5 rounded-full text-sm font-bold text-slate-600 hover:text-slate-950 hover:bg-white/50 transition-all duration-200"
                >
                  {item}
                </Link>
              ))}
            </div>

            {/* Right Action */}
            <Link to="/" className="flex items-center gap-2 text-slate-600 hover:text-slate-950 transition-colors">
              <span className="text-xs font-bold hidden sm:block">Back to Home</span>
              <ArrowLeft className="h-4 w-4" />
            </Link>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <div className="flex-1 flex items-center justify-center pt-24 pb-12 px-6 relative">
      <div className="absolute inset-0 z-0 pointer-events-none opacity-[0.03]" style={{ backgroundImage: 'radial-gradient(#000 1px, transparent 1px)', backgroundSize: '40px 40px' }} />

      <div className="w-full max-w-md relative z-10">
        {/* LOGO */}
        <div className="flex flex-col items-center mb-10">
          <div className="bg-slate-950 p-4 rounded-[2rem] shadow-lg mb-4">
            <Briefcase className="h-8 w-8 text-white" />
          </div>
          <h1 className="text-slate-950 text-2xl font-black tracking-tighter">JobTracker<span className="text-indigo-600">.</span></h1>
        </div>

        {/* AUTH CARD */}
        <div className="bg-white rounded-[2.5rem] shadow-[0_32px_64px_-15px_rgba(0,0,0,0.5)] p-10 border border-slate-100">
          <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
            <TabsList className="grid w-full grid-cols-3 mb-10 bg-slate-100 p-1.5 rounded-2xl">
              <TabsTrigger value="login" className="rounded-xl font-bold data-[state=active]:bg-white data-[state=active]:shadow-sm">Login</TabsTrigger>
              <TabsTrigger value="signup" className="rounded-xl font-bold data-[state=active]:bg-white data-[state=active]:shadow-sm">Sign Up</TabsTrigger>
              <TabsTrigger value="forgot" className="rounded-xl font-bold data-[state=active]:bg-white data-[state=active]:shadow-sm">Reset</TabsTrigger>
            </TabsList>

            {/* LOGIN TAB */}
            <TabsContent value="login" className="space-y-6 animate-in fade-in slide-in-from-bottom-2">
              <header>
                <h2 className="text-3xl font-black text-slate-950 tracking-tight">Welcome back</h2>
                <p className="text-slate-500 text-sm font-medium mt-1">Enter your details to access your dashboard</p>
              </header>

              {error && (
                <div className="bg-red-50 border border-red-200 rounded-2xl p-4">
                  <p className="text-red-600 text-sm font-medium">{error}</p>
                </div>
              )}

              <form onSubmit={handleLoginSubmit} className="space-y-5">
                <div className="space-y-2">
                  <Label className="text-[10px] uppercase font-black tracking-[0.2em] text-slate-400 ml-1">Email Address</Label>
                  <div className="relative">
                    <Mail className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400" />
                    <Input
                      type="email"
                      placeholder="name@company.com"
                      value={loginData.email}
                      onChange={(e) => setLoginData({ ...loginData, email: e.target.value })}
                      className="pl-12 h-14 rounded-2xl border-slate-200 bg-slate-50/50 focus:bg-white transition-all font-medium"
                      required
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label className="text-[10px] uppercase font-black tracking-[0.2em] text-slate-400 ml-1">Password</Label>
                  <div className="relative">
                    <Lock className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400" />
                    <Input
                      type={showPassword ? "text" : "password"}
                      placeholder="••••••••"
                      value={loginData.password}
                      onChange={(e) => setLoginData({ ...loginData, password: e.target.value })}
                      className="pl-12 pr-12 h-14 rounded-2xl border-slate-200 bg-slate-50/50 focus:bg-white transition-all font-medium"
                      required
                    />
                    <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-950">
                      {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>

                <Button 
                  type="submit" 
                  disabled={isLoading}
                  className="w-full h-14 bg-slate-950 hover:bg-indigo-600 text-white font-bold rounded-2xl transition-all shadow-xl shadow-slate-200 text-base disabled:opacity-50"
                >
                  {isLoading ? "Signing In..." : "Sign In"}
                </Button>
              </form>

              <div className="relative flex items-center gap-4 py-2">
                <div className="flex-1 h-px bg-slate-100" />
                <span className="text-[10px] font-black uppercase tracking-widest text-slate-300">Social Login</span>
                <div className="flex-1 h-px bg-slate-100" />
              </div>

              <Button onClick={() => console.log('Google')} variant="outline" className="w-full h-14 border-slate-200 rounded-2xl font-bold flex gap-3 hover:bg-slate-50">
                <svg className="h-5 w-5" viewBox="0 0 24 24"><path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/><path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/><path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/><path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/></svg>
                Continue with Google
              </Button>
            </TabsContent>

            {/* SIGN UP TAB */}
            <TabsContent value="signup" className="space-y-6 animate-in fade-in slide-in-from-bottom-2">
               <header>
                <h2 className="text-3xl font-black text-slate-950 tracking-tight">Create account</h2>
                <p className="text-slate-500 text-sm font-medium mt-1">Start your journey with us today</p>
              </header>
              
              {error && (
                <div className="bg-red-50 border border-red-200 rounded-2xl p-4">
                  <p className="text-red-600 text-sm font-medium">{error}</p>
                </div>
              )}
              
              <form onSubmit={handleSignupSubmit} className="space-y-4">
                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label className="text-[10px] uppercase font-black tracking-[0.2em] text-slate-400 ml-1">First Name</Label>
                    <div className="relative">
                      <User className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400" />
                      <Input 
                        placeholder="John" 
                        value={signupData.firstName}
                        className="pl-12 h-14 rounded-2xl border-slate-200 bg-slate-50/50 focus:bg-white transition-all font-medium"
                        onChange={(e) => setSignupData({...signupData, firstName: e.target.value})}
                        required
                      />
                    </div>
                  </div>
                  <div className="space-y-2">
                    <Label className="text-[10px] uppercase font-black tracking-[0.2em] text-slate-400 ml-1">Last Name</Label>
                    <div className="relative">
                      <User className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400" />
                      <Input 
                        placeholder="Doe" 
                        value={signupData.lastName}
                        className="pl-12 h-14 rounded-2xl border-slate-200 bg-slate-50/50 focus:bg-white transition-all font-medium"
                        onChange={(e) => setSignupData({...signupData, lastName: e.target.value})}
                        required
                      />
                    </div>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label className="text-[10px] uppercase font-black tracking-[0.2em] text-slate-400 ml-1">Role</Label>
                  <div className="relative">
                    <div className="relative">
                      <select
                        value={role}
                        onChange={(e) => setRole(e.target.value)}
                        className="w-full h-14 pl-12 pr-12 rounded-2xl border border-slate-200 bg-slate-50/50 focus:bg-white transition-all font-medium appearance-none cursor-pointer"
                      >
                        <option value="CANDIDATE">Job Seeker</option>
                        <option value="RECRUITER">Recruiter</option>
                        <option value="ADMIN">Admin</option>
                      </select>
                      <Briefcase className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400" />
                      <ChevronDown className="absolute right-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400 pointer-events-none" />
                    </div>
                  </div>
                </div>

                <div className="space-y-2">
                   <Label className="text-[10px] uppercase font-black tracking-[0.2em] text-slate-400 ml-1">Email Address</Label>
                  <div className="relative">
                    <Mail className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400" />
                    <Input 
                      type="email" 
                      placeholder="name@company.com" 
                      value={signupData.email}
                      className="pl-12 h-14 rounded-2xl border-slate-200 bg-slate-50/50 focus:bg-white transition-all font-medium"
                      onChange={(e) => setSignupData({...signupData, email: e.target.value})}
                      required
                    />
                  </div>
                </div>
                <div className="space-y-2">
                   <Label className="text-[10px] uppercase font-black tracking-[0.2em] text-slate-400 ml-1">Password</Label>
                   <div className="relative">
                    <Lock className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400" />
                    <Input 
                      type={showPassword ? "text" : "password"}
                      placeholder="••••••••" 
                      value={signupData.password}
                      className="pl-12 pr-12 h-14 rounded-2xl border-slate-200 bg-slate-50/50 focus:bg-white transition-all font-medium"
                      onChange={(e) => {
                        setSignupData({...signupData, password: e.target.value});
                        setPasswordErrors(checkPasswordRequirements(e.target.value));
                      }}
                      required
                    />
                    <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-950">
                      {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                   <div className="text-xs ml-1 space-y-1">
                     <p className="text-slate-500">Password must include:</p>
                     <div className="space-y-1">
                       <div className={`flex items-center gap-2 ${passwordErrors.includes("At least 8 characters") ? "text-red-500" : "text-green-600"}`}>
                         {passwordErrors.includes("At least 8 characters") ? "✗" : "✓"} At least 8 characters
                       </div>
                       <div className={`flex items-center gap-2 ${passwordErrors.includes("One uppercase letter") ? "text-red-500" : "text-green-600"}`}>
                         {passwordErrors.includes("One uppercase letter") ? "✗" : "✓"} One uppercase letter
                       </div>
                       <div className={`flex items-center gap-2 ${passwordErrors.includes("One special character") ? "text-red-500" : "text-green-600"}`}>
                         {passwordErrors.includes("One special character") ? "✗" : "✓"} One special character (!@#$%^&* etc.)
                       </div>
                     </div>
                   </div>
                </div>
                <div className="space-y-2">
                   <Label className="text-[10px] uppercase font-black tracking-[0.2em] text-slate-400 ml-1">Confirm Password</Label>
                  <div className="relative">
                    <Lock className="absolute left-4 top-1/2 -translate-y-1/2 h-5 w-5 text-slate-400" />
                    <Input 
                      type={showConfirmPassword ? "text" : "password"}
                      placeholder="••••••••" 
                      value={signupData.confirmPassword}
                      className="pl-12 pr-12 h-14 rounded-2xl border-slate-200 bg-slate-50/50 focus:bg-white transition-all font-medium"
                      onChange={(e) => setSignupData({...signupData, confirmPassword: e.target.value})}
                      required
                    />
                    <button type="button" onClick={() => setShowConfirmPassword(!showConfirmPassword)} className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-950">
                      {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>
                <Button 
                  type="submit" 
                  disabled={isLoading}
                  className="w-full h-14 bg-slate-950 hover:bg-indigo-600 text-white font-bold rounded-2xl mt-4 disabled:opacity-50"
                >
                  {isLoading ? "Creating Account..." : "Create Account"}
                </Button>
              </form>
            </TabsContent>

            {/* FORGOT TAB */}
            <TabsContent value="forgot" className="space-y-6 animate-in fade-in slide-in-from-bottom-2">
              <header>
                <h2 className="text-3xl font-black text-slate-950 tracking-tight">Lost access?</h2>
                <p className="text-slate-500 text-sm font-medium mt-1">Enter your email for a recovery link</p>
              </header>
              <div className="space-y-4">
                <Input 
                  placeholder="your@email.com" 
                  className="h-14 rounded-2xl border-slate-200 bg-slate-50/50 px-4 font-medium" 
                />
                <Button className="w-full h-14 bg-slate-950 hover:bg-indigo-600 text-white font-bold rounded-2xl">
                  Send Link
                </Button>
              </div>
            </TabsContent>
          </Tabs>
        </div>

        {/* FOOTER */}
        <div className="text-center text-slate-500 text-[10px] font-bold uppercase tracking-widest mt-10 space-y-2">
          <p>© 2026 JobTracker • <a href="#" className="text-slate-600 hover:text-slate-950 transition-colors">Privacy Policy</a> • <a href="#" className="text-slate-600 hover:text-slate-950 transition-colors">Terms of Service</a></p>
        </div>
      </div>
      </div>

      {/* BOTTOM FOOTER */}
      <footer className="relative z-10 bg-white/50 backdrop-blur-sm border-t border-slate-200 py-8 mt-auto">
        <div className="max-w-7xl mx-auto px-6">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
            {/* Brand */}
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <div className="bg-slate-950 p-2 rounded-lg">
                  <Briefcase className="h-4 w-4 text-white" />
                </div>
                <span className="font-black text-slate-950 text-sm">JobTracker</span>
              </div>
              <p className="text-xs text-slate-600">The intelligent way to land your next big role.</p>
            </div>

            {/* Product */}
            <div className="space-y-3">
              <h4 className="text-xs font-black uppercase tracking-[0.2em] text-slate-600">Product</h4>
              <ul className="space-y-2 text-xs text-slate-600">
                <li><a href="#" className="hover:text-slate-950 transition-colors">For Job Seekers</a></li>
                <li><a href="#" className="hover:text-slate-950 transition-colors">For Employers</a></li>
                <li><a href="#" className="hover:text-slate-950 transition-colors">Pricing</a></li>
              </ul>
            </div>

            {/* Company */}
            <div className="space-y-3">
              <h4 className="text-xs font-black uppercase tracking-[0.2em] text-slate-600">Company</h4>
              <ul className="space-y-2 text-xs text-slate-600">
                <li><a href="#" className="hover:text-slate-950 transition-colors">About</a></li>
                <li><a href="#" className="hover:text-slate-950 transition-colors">Blog</a></li>
                <li><a href="#" className="hover:text-slate-950 transition-colors">Careers</a></li>
              </ul>
            </div>

            {/* Legal */}
            <div className="space-y-3">
              <h4 className="text-xs font-black uppercase tracking-[0.2em] text-slate-600">Legal</h4>
              <ul className="space-y-2 text-xs text-slate-600">
                <li><a href="#" className="hover:text-slate-950 transition-colors">Privacy</a></li>
                <li><a href="#" className="hover:text-slate-950 transition-colors">Terms</a></li>
                <li><a href="#" className="hover:text-slate-950 transition-colors">Contact</a></li>
              </ul>
            </div>
          </div>

          {/* Bottom */}
          <div className="border-t border-slate-200 pt-8 flex flex-col md:flex-row justify-between items-center gap-4">
            <p className="text-xs text-slate-600">© 2026 JobTracker. All rights reserved.</p>
            <div className="flex gap-6 text-slate-600 text-xs">
              <a href="#" className="hover:text-slate-950 transition-colors">Twitter</a>
              <a href="#" className="hover:text-slate-950 transition-colors">LinkedIn</a>
              <a href="#" className="hover:text-slate-950 transition-colors">GitHub</a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}