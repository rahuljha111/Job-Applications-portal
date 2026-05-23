import React from 'react';
import { useAuth } from '../contexts/AuthContext';
import { Button } from '../comoponents/ui/button';
import { useNavigate } from 'react-router';
import { Briefcase, LogOut, User } from 'lucide-react';

export function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
  };

  return (
    <div className="min-h-screen bg-[#fafafa]">
      {/* Header */}
      <header className="bg-white border-b border-slate-200 shadow-sm">
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="bg-slate-950 p-2 rounded-xl">
                <Briefcase className="h-6 w-6 text-white" />
              </div>
              <h1 className="text-2xl font-black text-slate-950">
                JobTracker<span className="text-indigo-600">.</span>
              </h1>
            </div>
            
            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2 text-slate-600">
                <User className="h-5 w-5" />
                <span className="font-medium">
                  {user?.firstName} {user?.lastName}
                </span>
                <span className="px-2 py-1 bg-slate-100 rounded-full text-xs font-bold text-slate-600">
                  {user?.role}
                </span>
              </div>
              <Button 
                onClick={handleLogout}
                variant="outline"
                size="sm"
                className="flex items-center gap-2"
              >
                <LogOut className="h-4 w-4" />
                Logout
              </Button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-6 py-8">
        <div className="mb-8">
          <h2 className="text-3xl font-black text-slate-950 mb-2">
            Welcome back, {user?.firstName}! 👋
          </h2>
          <p className="text-slate-600">
            Here's what's happening with your job applications today.
          </p>
        </div>

        {/* Dashboard Content based on role */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {user?.role === 'CANDIDATE' && (
            <>
              <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
                <h3 className="text-lg font-bold text-slate-950 mb-2">Applications</h3>
                <div className="text-3xl font-black text-indigo-600 mb-2">0</div>
                <p className="text-slate-500 text-sm">Total applications submitted</p>
              </div>
              
              <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
                <h3 className="text-lg font-bold text-slate-950 mb-2">Interviews</h3>
                <div className="text-3xl font-black text-green-600 mb-2">0</div>
                <p className="text-slate-500 text-sm">Upcoming interviews</p>
              </div>
              
              <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
                <h3 className="text-lg font-bold text-slate-950 mb-2">Offers</h3>
                <div className="text-3xl font-black text-orange-600 mb-2">0</div>
                <p className="text-slate-500 text-sm">Job offers received</p>
              </div>
            </>
          )}
          
          {user?.role === 'RECRUITER' && (
            <>
              <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
                <h3 className="text-lg font-bold text-slate-950 mb-2">Job Postings</h3>
                <div className="text-3xl font-black text-blue-600 mb-2">0</div>
                <p className="text-slate-500 text-sm">Active job postings</p>
              </div>
              
              <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
                <h3 className="text-lg font-bold text-slate-950 mb-2">Candidates</h3>
                <div className="text-3xl font-black text-green-600 mb-2">0</div>
                <p className="text-slate-500 text-sm">Total applications received</p>
              </div>
              
              <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
                <h3 className="text-lg font-bold text-slate-950 mb-2">Hires</h3>
                <div className="text-3xl font-black text-purple-600 mb-2">0</div>
                <p className="text-slate-500 text-sm">Successful hires this month</p>
              </div>
            </>
          )}
          
          {user?.role === 'ADMIN' && (
            <>
              <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
                <h3 className="text-lg font-bold text-slate-950 mb-2">Users</h3>
                <div className="text-3xl font-black text-indigo-600 mb-2">1</div>
                <p className="text-slate-500 text-sm">Total registered users</p>
              </div>
              
              <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
                <h3 className="text-lg font-bold text-slate-950 mb-2">Jobs</h3>
                <div className="text-3xl font-black text-blue-600 mb-2">0</div>
                <p className="text-slate-500 text-sm">Total job postings</p>
              </div>
              
              <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
                <h3 className="text-lg font-bold text-slate-950 mb-2">Companies</h3>
                <div className="text-3xl font-black text-green-600 mb-2">0</div>
                <p className="text-slate-500 text-sm">Registered companies</p>
              </div>
            </>
          )}
        </div>

        <div className="mt-8 bg-white rounded-2xl p-6 border border-slate-200 shadow-sm">
          <h3 className="text-xl font-bold text-slate-950 mb-4">Quick Actions</h3>
          <div className="flex flex-wrap gap-3">
            {user?.role === 'CANDIDATE' && (
              <>
                <Button
                  className="bg-slate-950 hover:bg-indigo-600 text-white"
                  onClick={() => navigate('/job-seeker')}
                >
                  Browse Jobs
                </Button>
                <Button
                  variant="outline"
                  onClick={() => navigate('/job-seeker/settings')}
                >
                  Update Profile
                </Button>
                <Button
                  variant="outline"
                  onClick={() => navigate('/job-seeker/resume')}
                >
                  Upload Resume
                </Button>
              </>
            )}
            
            {user?.role === 'RECRUITER' && (
              <>
                <Button className="bg-slate-950 hover:bg-indigo-600 text-white">
                  Post New Job
                </Button>
                <Button variant="outline">
                  View Applications
                </Button>
                <Button variant="outline">
                  Manage Company
                </Button>
              </>
            )}
            
            {user?.role === 'ADMIN' && (
              <>
                <Button className="bg-slate-950 hover:bg-indigo-600 text-white">
                  User Management
                </Button>
                <Button variant="outline">
                  System Analytics
                </Button>
                <Button variant="outline">
                  Platform Settings
                </Button>
              </>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}