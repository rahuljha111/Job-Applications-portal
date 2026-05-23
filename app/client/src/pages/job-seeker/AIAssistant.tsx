import { PortalTopbar } from "../../comoponents/PortalTopbar";
import { Card, CardContent, CardHeader, CardTitle } from "../../comoponents/ui/card";
import { Button } from "../../comoponents/ui/button";
import { Badge } from "../../comoponents/ui/badge";
import { Textarea } from "../../comoponents/ui/textarea";
import { Sparkles, Send, FileText, Briefcase, MessageSquare, TrendingUp } from "lucide-react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../../comoponents/ui/tabs";
import { useState } from "react";
import apiService from "../../services/api";
import { toast } from "sonner";

const aiFeatures = [
  {
    icon: FileText,
    title: "Resume Review",
    description: "Get AI-powered feedback on your resume",
    color: "bg-blue-500",
  },
  {
    icon: Briefcase,
    title: "Job Match",
    description: "Find jobs that match your profile",
    color: "bg-green-500",
  },
  {
    icon: MessageSquare,
    title: "Interview Prep",
    description: "Practice with AI-generated questions",
    color: "bg-purple-500",
  },
  {
    icon: TrendingUp,
    title: "Career Advice",
    description: "Get personalized career guidance",
    color: "bg-orange-500",
  },
];

const conversationHistory = [
  {
    role: "user",
    message: "Can you review my resume for a Senior React Developer position?",
    timestamp: "2:34 PM",
  },
  {
    role: "ai",
    message: "I'd be happy to review your resume! Based on the Senior React Developer position, here are my key observations:\n\n✅ Strong technical skills section\n✅ Good project descriptions with metrics\n\n⚠️ Consider adding:\n- More emphasis on TypeScript experience\n- Leadership and mentoring examples\n- Performance optimization achievements\n\nWould you like me to provide specific suggestions for any section?",
    timestamp: "2:34 PM",
  },
];

export function AIAssistant() {
  const [input, setInput] = useState("");
  const [conversation, setConversation] = useState(conversationHistory);

  const handleSend = async () => {
    if (!input.trim()) return;
    // add user message
    const userMsg = { role: "user", message: input, timestamp: new Date().toLocaleTimeString() };
    setConversation((c) => [...c, userMsg]);
    setInput("");

    try {
      const resp = await apiService.getInterviewPrep({ jobTitle: input });
      const aiTextParts: string[] = [];
      if (resp.technicalQuestions) {
        aiTextParts.push("Technical Questions:\n" + resp.technicalQuestions.map((q: any) => `- ${q.question}`).join("\n"));
      }
      if (resp.preparationTips) {
        aiTextParts.push("Tips:\n" + resp.preparationTips.join("\n"));
      }

      const aiMsg = { role: "ai", message: aiTextParts.join("\n\n") || "I couldn't generate suggestions.", timestamp: new Date().toLocaleTimeString() };
      setConversation((c) => [...c, aiMsg]);
    } catch (err: any) {
      console.error(err);
      toast.error(err?.message || "AI request failed");
    }
  };

  return (
    <>
      <PortalTopbar title="AI Assistant" subtitle="Get personalized AI-powered career guidance" />
      <main className="flex-1 overflow-auto p-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Main Chat Area */}
          <div className="lg:col-span-2">
            <Card className="h-[calc(100vh-12rem)]">
              <CardHeader className="flex flex-row items-center gap-2 border-b">
                <Sparkles className="h-5 w-5 text-green-500" />
                <CardTitle>AI Career Assistant</CardTitle>
                <Badge variant="secondary" className="ml-2 bg-green-100 text-green-700">
                  AI Powered
                </Badge>
              </CardHeader>
              <CardContent className="flex flex-col h-full p-0">
                {/* Chat Messages */}
                <div className="flex-1 overflow-auto p-6 space-y-4">
                  {conversationHistory.map((msg, index) => (
                    <div
                      key={index}
                      className={`flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}
                    >
                      <div
                        className={`max-w-[80%] rounded-lg p-4 ${
                          msg.role === "user"
                            ? "bg-green-500 text-white"
                            : "bg-gray-100 text-gray-900"
                        }`}
                      >
                        <p className="text-sm whitespace-pre-wrap">{msg.message}</p>
                        <p className={`text-xs mt-2 ${msg.role === "user" ? "text-green-100" : "text-gray-500"}`}>
                          {msg.timestamp}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>

                {/* Input Area */}
                <div className="border-t p-4">
                  <div className="flex gap-2">
                    <Textarea
                      placeholder="Ask me anything about your job search, resume, or career..."
                      className="resize-none"
                      rows={3}
                    />
                    <Button className="bg-green-500 hover:bg-green-600">
                      <Send className="h-4 w-4" />
                    </Button>
                  </div>
                  <p className="text-xs text-gray-500 mt-2">
                    AI can make mistakes. Verify important information.
                  </p>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Sidebar with AI Features */}
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>AI Features</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {aiFeatures.map((feature, index) => (
                    <button
                      key={index}
                      className="w-full p-3 rounded-lg border hover:border-green-500 transition-colors text-left"
                    >
                      <div className="flex items-start gap-3">
                        <div className={`p-2 rounded-lg ${feature.color} bg-opacity-10`}>
                          <feature.icon className={`h-4 w-4 ${feature.color.replace("bg-", "text-")}`} />
                        </div>
                        <div className="flex-1">
                          <h4 className="font-medium text-sm mb-1">{feature.title}</h4>
                          <p className="text-xs text-gray-500">{feature.description}</p>
                        </div>
                      </div>
                    </button>
                  ))}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Quick Prompts</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                  <Button variant="outline" className="w-full justify-start text-sm" size="sm">
                    Review my resume
                  </Button>
                  <Button variant="outline" className="w-full justify-start text-sm" size="sm">
                    Find matching jobs
                  </Button>
                  <Button variant="outline" className="w-full justify-start text-sm" size="sm">
                    Prepare for interview
                  </Button>
                  <Button variant="outline" className="w-full justify-start text-sm" size="sm">
                    Write cover letter
                  </Button>
                  <Button variant="outline" className="w-full justify-start text-sm" size="sm">
                    Career advice
                  </Button>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Usage Stats</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">AI Requests Today</span>
                    <span className="font-medium">12 / 50</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Monthly Limit</span>
                    <span className="font-medium">156 / 500</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </main>
    </>
  );
}
