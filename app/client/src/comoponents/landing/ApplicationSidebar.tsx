import { useState, useRef, useEffect } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "../ui/sheet";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Label } from "../ui/label";
import { Textarea } from "../ui/textarea";
import {
  Upload,
  X,
  FileText,
  CheckCircle2,
  AlertCircle,
  Loader2,
  Briefcase,
  Globe,
} from "lucide-react";
import { toast } from "sonner";
import apiService from "../../services/api";
import type { JobListing } from "../../data/mockJobs";
import { cn } from "@/lib/utils";

const applicationSchema = z.object({
  fullName: z.string().min(2, "Name must be at least 2 characters"),
  email: z.string().email("Invalid email address"),
  phone: z.string().min(10, "Phone number must be at least 10 digits"),
  portfolioUrl: z.string().url("Invalid URL").optional().or(z.literal("")),
  coverLetter: z.string().min(50, "Cover letter must be at least 50 characters"),
});

type ApplicationFormData = z.infer<typeof applicationSchema>;

interface ApplicationSidebarProps {
  isOpen: boolean;
  onClose: () => void;
  job: JobListing | null;
}

export function ApplicationSidebar({ isOpen, onClose, job }: ApplicationSidebarProps) {
  const [resumeFile, setResumeFile] = useState<any>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const resumeInputRef = useRef<HTMLInputElement>(null);

  const { register, handleSubmit, formState: { errors }, reset, setValue, watch } = useForm<ApplicationFormData>({
    resolver: zodResolver(applicationSchema),
  });

  const onSubmit = async (data: ApplicationFormData) => {
    if (!resumeFile) return toast.error("Resume is required");
    setIsSubmitting(true);
    try {
      // 1) Upload resume
      let resumeResp: any = null;
      try {
        resumeResp = await apiService.uploadResume(resumeFile);
        toast.success("Resume uploaded");
      } catch (err) {
        console.warn("Resume upload failed, continuing to apply:", err);
      }

      // 2) Submit application
      const payload: any = {
        coverLetter: data.coverLetter,
        portfolioUrl: data.portfolioUrl,
      };
      if (resumeResp && (resumeResp.id || resumeResp.resumeId)) {
        payload.resumeId = resumeResp.id || resumeResp.resumeId;
      }

      await apiService.applyToJob(job.id, payload);
      toast.success("Application submitted successfully");
      onClose();
    } catch (err: any) {
      console.error("Application failed:", err);
      toast.error(err?.message || "Failed to submit application");
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!job) return null;

  return (
    <Sheet open={isOpen} onOpenChange={onClose}>
      {/* FIX: Explicit bg-white and shadow-2xl for visibility */}
      <SheetContent className="w-full sm:max-w-xl p-0 border-l border-slate-200 bg-white shadow-2xl">
        <div className="h-full flex flex-col">
          
          {/* Header Section: Sticky with a subtle gradient */}
          <div className="p-8 bg-slate-50 border-b border-slate-100">
            <SheetHeader className="space-y-4">
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 rounded-xl bg-indigo-600 flex items-center justify-center shadow-lg shadow-indigo-100">
                  <Briefcase className="text-white h-6 w-6" />
                </div>
                <div>
                  <SheetTitle className="text-2xl font-black tracking-tight text-slate-900">Apply for Role</SheetTitle>
                  <SheetDescription className="text-indigo-600 font-bold flex items-center gap-2">
                    {job.title} <span className="text-slate-300">•</span> {job.company}
                  </SheetDescription>
                </div>
              </div>
            </SheetHeader>
          </div>

          {/* Form Content: Scrollable Area */}
          <div className="flex-1 overflow-y-auto p-8">
            <form id="app-form" onSubmit={handleSubmit(onSubmit)} className="space-y-8">
              
              {/* Personal Info Section */}
              <section className="space-y-4">
                <h4 className="text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Personal Details</h4>
                <div className="grid grid-cols-1 gap-4">
                  <div className="space-y-2">
                    <Label className="font-bold text-slate-700">Full Name</Label>
                    <Input {...register("fullName")} className="h-12 rounded-xl border-slate-200 focus:ring-indigo-500" placeholder="Cameron Williamson" />
                    {errors.fullName && <p className="text-xs text-red-500 font-medium">{errors.fullName.message}</p>}
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label className="font-bold text-slate-700">Email Address</Label>
                      <Input {...register("email")} type="email" className="h-12 rounded-xl border-slate-200" placeholder="cameron@work.com" />
                    </div>
                    <div className="space-y-2">
                      <Label className="font-bold text-slate-700">Phone</Label>
                      <Input {...register("phone")} className="h-12 rounded-xl border-slate-200" placeholder="+1 (555) 000-0000" />
                    </div>
                  </div>
                </div>
              </section>

              {/* Links Section */}
              <section className="space-y-4">
                 <h4 className="text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Professional Presence</h4>
                 <div className="relative group">
                    <Globe className="absolute left-4 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
                    <Input {...register("portfolioUrl")} className="pl-11 h-12 rounded-xl border-slate-200" placeholder="https://portfolio.me" />
                 </div>
              </section>

              {/* Resume Upload Section: Redesigned for impact */}
              <section className="space-y-4">
                <h4 className="text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Documentation</h4>
                <div
                  onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
                  onDragLeave={() => setIsDragging(false)}
                  onDrop={(e) => { e.preventDefault(); setIsDragging(false); /* file logic here */ }}
                  className={cn(
                    "relative group cursor-pointer border-2 border-dashed rounded-[1.5rem] p-8 transition-all duration-300",
                    isDragging ? "border-indigo-500 bg-indigo-50/50" : "border-slate-200 hover:border-indigo-300 hover:bg-slate-50/50"
                  )}
                  onClick={() => resumeInputRef.current?.click()}
                >
                  <input ref={resumeInputRef} type="file" className="hidden" onChange={(e) => setResumeFile(e.target.files?.[0])} />
                  
                  {resumeFile ? (
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 rounded-lg bg-indigo-100 flex items-center justify-center">
                        <FileText className="text-indigo-600 h-6 w-6" />
                      </div>
                      <div className="flex-1 overflow-hidden">
                        <p className="font-bold text-slate-900 truncate">{resumeFile.name}</p>
                        <p className="text-xs text-slate-500">{(resumeFile.size / 1024 / 1024).toFixed(2)} MB • Ready to send</p>
                      </div>
                      <Button variant="ghost" size="icon" onClick={(e) => { e.stopPropagation(); setResumeFile(null); }}>
                        <X className="h-4 w-4" />
                      </Button>
                    </div>
                  ) : (
                    <div className="text-center space-y-2">
                      <div className="mx-auto w-12 h-12 rounded-full bg-slate-100 flex items-center justify-center group-hover:scale-110 group-hover:bg-indigo-100 transition-all">
                        <Upload className="h-5 w-5 text-slate-400 group-hover:text-indigo-600" />
                      </div>
                      <p className="text-sm font-bold text-slate-700">Upload your Resume</p>
                      <p className="text-xs text-slate-400 uppercase tracking-widest font-black">PDF • DOCX • MAX 5MB</p>
                    </div>
                  )}
                </div>
              </section>

              {/* Cover Letter Section */}
              <section className="space-y-4">
                <h4 className="text-[10px] font-black uppercase tracking-[0.2em] text-slate-400">Introduction</h4>
                <div className="space-y-2">
                  <Label className="font-bold text-slate-700">Cover Letter</Label>
                  <Textarea 
                    {...register("coverLetter")} 
                    className="min-h-[160px] rounded-2xl border-slate-200 p-4 focus:ring-indigo-500 leading-relaxed" 
                    placeholder="Tell the hiring team why you're a perfect fit..."
                  />
                  {errors.coverLetter && <p className="text-xs text-red-500 font-medium">{errors.coverLetter.message}</p>}
                </div>
              </section>
            </form>
          </div>

          {/* Footer Section: Sticky CTA */}
          <div className="p-8 border-t border-slate-100 bg-white">
            <div className="flex gap-4">
              <Button variant="ghost" onClick={onClose} className="flex-1 h-14 rounded-2xl font-bold text-slate-500">
                Discard
              </Button>
              <Button 
                form="app-form"
                type="submit" 
                disabled={isSubmitting}
                className="flex-[2] h-14 rounded-2xl bg-indigo-600 hover:bg-indigo-700 shadow-xl shadow-indigo-100 font-bold group"
              >
                {isSubmitting ? (
                  <Loader2 className="h-5 w-5 animate-spin" />
                ) : (
                  <span className="flex items-center gap-2">
                    Submit Application
                    <CheckCircle2 className="h-5 w-5 group-hover:scale-110 transition-transform" />
                  </span>
                )}
              </Button>
            </div>
          </div>
        </div>
      </SheetContent>
    </Sheet>
  );
}