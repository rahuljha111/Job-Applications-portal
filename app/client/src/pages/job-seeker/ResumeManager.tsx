import { ChangeEvent, useEffect, useMemo, useRef, useState } from "react";
import { PortalTopbar } from "../../comoponents/PortalTopbar";
import { Card, CardContent, CardHeader, CardTitle } from "../../comoponents/ui/card";
import { Button } from "../../comoponents/ui/button";
import { Badge } from "../../comoponents/ui/badge";
import { Upload, FileText, Download, Eye, Edit, Trash2, Sparkles, Loader2 } from "lucide-react";
import { EmptyState } from "../../comoponents/EmptyState";
import apiService from "../../services/api";
import { toast } from "sonner";

const aiSuggestions = [
  { category: "Skills", suggestion: "Add more specific technologies (e.g., TypeScript, Next.js)", priority: "high" },
  { category: "Experience", suggestion: "Quantify achievements with metrics (e.g., 'Increased performance by 40%')", priority: "high" },
  { category: "Format", suggestion: "Use consistent date formatting throughout", priority: "medium" },
  { category: "Keywords", suggestion: "Include industry-standard keywords for better ATS compatibility", priority: "high" },
];

export function ResumeManager() {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [resumes, setResumes] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [analyzing, setAnalyzing] = useState(false);
  const [analysis, setAnalysis] = useState<any>(null);

  const loadResumes = async () => {
    setLoading(true);
    try {
      const response = await apiService.getResumes();
      const list = Array.isArray(response) ? response : (response as any)?.content || [];
      setResumes(list);
    } catch (err: any) {
      console.error(err);
      toast.error(err?.message || "Failed to load resumes");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void loadResumes();
  }, []);

  const primaryResume = useMemo(() => resumes.find((resume) => resume.default || resume.isDefault) || resumes[0] || null, [resumes]);

  const handleUploadClick = () => fileInputRef.current?.click();

  const handleFileSelected = async (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    try {
      await apiService.uploadResume(file, "Primary Resume");
      toast.success("Resume uploaded");
      await loadResumes();
    } catch (err: any) {
      console.error(err);
      toast.error(err?.message || "Resume upload failed");
    } finally {
      event.target.value = "";
    }
  };

  const handleAnalyze = async () => {
    if (!primaryResume?.id) {
      toast.error("Upload a resume first");
      return;
    }

    setAnalyzing(true);
    setAnalysis(null);
    try {
      const response = await apiService.analyzeResume({
        resumeId: primaryResume.id,
        targetRole: "Software Engineer",
      });
      setAnalysis(response);
      toast.success("Resume analyzed");
    } catch (err: any) {
      console.error(err);
      toast.error(err?.message || "Resume analysis failed");
    } finally {
      setAnalyzing(false);
    }
  };

  return (
    <>
      <PortalTopbar title="Resume Manager" subtitle="Manage and optimize your resumes" />
      <main className="flex-1 overflow-auto p-6">
        <input ref={fileInputRef} type="file" accept=".pdf,.doc,.docx" className="hidden" onChange={handleFileSelected} />
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Resume List */}
          <div className="lg:col-span-2 space-y-6">
            <Card>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle>My Resumes</CardTitle>
                  <Button className="bg-green-500 hover:bg-green-600" onClick={handleUploadClick}>
                    <Upload className="h-4 w-4 mr-2" />
                    Upload Resume
                  </Button>
                </div>
              </CardHeader>
              <CardContent>
                {loading ? (
                  <div className="flex items-center justify-center py-12 text-gray-500 gap-2">
                    <Loader2 className="h-4 w-4 animate-spin" />
                    Loading resumes...
                  </div>
                ) : resumes.length > 0 ? (
                  <div className="space-y-4">
                    {resumes.map((resume) => (
                      <div key={resume.id} className="p-4 rounded-lg border hover:shadow-md transition-shadow">
                        <div className="flex items-start justify-between">
                          <div className="flex items-start gap-3 flex-1">
                            <div className="p-2 rounded-lg bg-green-100">
                              <FileText className="h-5 w-5 text-green-600" />
                            </div>
                            <div className="flex-1">
                              <div className="flex items-center gap-2 mb-1">
                                <h4 className="font-medium">{resume.originalFileName || resume.label || resume.fileName}</h4>
                                {(resume.default || resume.isDefault) && (
                                  <Badge variant="secondary" className="bg-green-100 text-green-700">
                                    Primary
                                  </Badge>
                                )}
                              </div>
                              <div className="flex items-center gap-3 text-sm text-gray-500 mb-2">
                                <span>Uploaded: {resume.uploadedAt ? new Date(resume.uploadedAt).toLocaleDateString() : "Just now"}</span>
                                <span>•</span>
                                <span>{resume.fileSize ? `${Math.round(resume.fileSize / 1024)} KB` : "N/A"}</span>
                              </div>
                              <div className="flex items-center gap-2">
                                <Sparkles className="h-4 w-4 text-green-500" />
                                <span className="text-sm">AI Score: </span>
                                <Badge variant="secondary" className="bg-green-100 text-green-700">
                                  {analysis?.matchScore ?? "--"}/100
                                </Badge>
                              </div>
                            </div>
                          </div>
                          <div className="flex items-center gap-2">
                            <Button variant="outline" size="sm">
                              <Eye className="h-4 w-4" />
                            </Button>
                            <Button variant="outline" size="sm">
                              <Download className="h-4 w-4" />
                            </Button>
                            <Button variant="outline" size="sm">
                              <Edit className="h-4 w-4" />
                            </Button>
                            <Button variant="ghost" size="sm">
                              <Trash2 className="h-4 w-4 text-red-500" />
                            </Button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <EmptyState
                    icon={FileText}
                    title="No resumes uploaded"
                    description="Upload your first resume to get AI-powered insights and recommendations"
                    action={{ label: "Upload Resume", onClick: handleUploadClick }}
                  />
                )}
              </CardContent>
            </Card>

            {/* Resume Templates */}
            <Card>
              <CardHeader>
                <CardTitle>Resume Templates</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="p-4 rounded-lg border text-center hover:border-green-500 cursor-pointer transition-colors">
                    <div className="h-32 bg-gray-100 rounded mb-3 flex items-center justify-center">
                      <FileText className="h-12 w-12 text-gray-400" />
                    </div>
                    <h4 className="font-medium mb-1">Professional</h4>
                    <p className="text-xs text-gray-500">Classic ATS-friendly layout</p>
                  </div>
                  <div className="p-4 rounded-lg border text-center hover:border-green-500 cursor-pointer transition-colors">
                    <div className="h-32 bg-gray-100 rounded mb-3 flex items-center justify-center">
                      <FileText className="h-12 w-12 text-gray-400" />
                    </div>
                    <h4 className="font-medium mb-1">Modern</h4>
                    <p className="text-xs text-gray-500">Clean and contemporary</p>
                  </div>
                  <div className="p-4 rounded-lg border text-center hover:border-green-500 cursor-pointer transition-colors">
                    <div className="h-32 bg-gray-100 rounded mb-3 flex items-center justify-center">
                      <FileText className="h-12 w-12 text-gray-400" />
                    </div>
                    <h4 className="font-medium mb-1">Creative</h4>
                    <p className="text-xs text-gray-500">Stand out with design</p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* AI Suggestions Sidebar */}
          <div>
            <Card>
              <CardHeader className="flex flex-row items-center gap-2">
                <Sparkles className="h-5 w-5 text-green-500" />
                <CardTitle>AI Suggestions</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {aiSuggestions.map((suggestion, index) => (
                    <div key={index} className="p-3 rounded-lg border bg-green-50">
                      <div className="flex items-start justify-between mb-2">
                        <Badge 
                          variant="secondary" 
                          className={
                            suggestion.priority === "high" 
                              ? "bg-red-100 text-red-700" 
                              : "bg-yellow-100 text-yellow-700"
                          }
                        >
                          {suggestion.priority}
                        </Badge>
                        <span className="text-xs font-medium text-green-700">{suggestion.category}</span>
                      </div>
                      <p className="text-sm">{suggestion.suggestion}</p>
                    </div>
                  ))}
                </div>
                <Button className="w-full mt-4 bg-green-500 hover:bg-green-600" onClick={handleAnalyze} disabled={analyzing}>
                  <Sparkles className="h-4 w-4 mr-2" />
                  {analyzing ? "Analyzing..." : "Optimize Resume"}
                </Button>

                {analysis && (
                  <div className="mt-4 rounded-lg border bg-white p-4 space-y-2 text-sm">
                    <div className="flex items-center justify-between">
                      <span className="font-medium">Match score</span>
                      <span className="font-bold text-green-600">{analysis.matchScore}/100</span>
                    </div>
                    {analysis.summary && <p className="text-gray-600">{analysis.summary}</p>}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
    </>
  );
}
