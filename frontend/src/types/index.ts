export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  registrationDate: string;
  role: 'USER' | 'ADVISOR' | 'ADMIN';
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
}

export interface UserProfile {
  id: number;
  age?: number;
  occupation?: string;
  educationLevel?: 'HIGH_SCHOOL' | 'ASSOCIATE' | 'BACHELOR' | 'MASTER' | 'DOCTORATE' ;
  monthlyIncome?: number;
  monthlyExpenses?: number;
  totalSavings?: number;
  totalDebt?: number;
  riskTolerance?: number;
  investmentHorizon?: string;
  currentInvestmentsJson?: string;
  investmentInterestsJson?: string;
  financialGoalsJson?: string;
  retirementAge?: number;
  monthlySavings?: number;
}

export interface Advisor {
  firstName: string;
  lastName:string
  id: number;
  user: User;
  professionalTitle: string;
  experienceYears: number;
  bio: string;
  hourlyRate: number;
  profileImageUrl: string;
  specialties: string[];
  languages: string[];
  averageRating: number;
  ratingCount: number;
}

export interface AvailabilitySlot {
  id: number;
  dayOfWeek?: number;
  startTime: string;
  endTime: string;
  recurring: boolean;
  specificDate?: string;
}

export interface FinancialPlan {
  id: number;
  creationDate: string;
  planName: string;
  healthScore: number;
  healthAssessment: string;
  shareCode: string;
  assetAllocationJson: string;
  goalTimelineJson: string;
  investmentRecommendationsJson: string;
  actionPlanJson: string;
}

export interface Appointment {
  id: number;
  user: User;
  advisor: Advisor;
  appointmentDate: string;
  durationMinutes: number;
  sessionType: 'INITIAL_CONSULTATION' | 'STANDARD_SESSION' | 'FOLLOWUP_SESSION' | 'PLAN_REVIEW';
  status: 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';
  bookingDate: string;
  meetingLink?: string;
  sharedPlan?: FinancialPlan;
  advisorNotes?: string;
  userNotes?: string;
}
export interface AppointmentDetails {
  id: number;
  appointmentDate: string;
  durationMinutes: number;
  sessionType: 'INITIAL_CONSULTATION' | 'STANDARD_SESSION' | 'FOLLOWUP_SESSION' | 'PLAN_REVIEW';
  status:  'CONFIRMED' | 'CANCELLED' | 'COMPLETED';
  meetingLink?: string;
  userNotes?: string;
  advisorNotes?: string;

  // 平铺的顾问信息
  advisorId: number;
  advisorFirstName: string;
  advisorLastName: string;
  advisorProfessionalTitle: string;
  advisorSpecialties: string[];

  // 平铺的共享计划信息（可选）
  sharedPlanId?: number;
  sharedPlanName?: string;
  sharedPlanCreationDate?: string;
  sharedPlanHealthAssessment?: string;
}

// Request/Response Types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface JwtResponse {
  token: string;
  id: number;
  email: string;
  role: string;
}

export interface UserProfileUpdateDto {
  age?: number;
  occupation?: string;
  educationLevel?: 'HIGH_SCHOOL' | 'ASSOCIATE' | 'BACHELOR' | 'MASTER' | 'DOCTORATE';
  monthlyIncome?: number;
  monthlyExpenses?: number;
  totalSavings?: number;
  totalDebt?: number;
  riskTolerance?: number;
  investmentHorizon?: string;
  currentInvestmentsJson?: string;
  investmentInterestsJson?: string;
  financialGoalsJson?: string;
  retirementAge?: number;
  monthlySavings?: number;
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
}

export interface FinancialPlanRequest {
  planName: string;
  additionalInstructions?: string;
}

export interface AdvisorProfileDto {
  professionalTitle?: string;
  experienceYears?: number;
  bio?: string;
  profileImageUrl?: string;
  specialties?: string[];
  languages?: string[];
}

export interface AvailabilitySlotDto {
  dayOfWeek?: number;
  startTime: string;
  endTime: string;
  recurring: boolean;
  specificDate?: string;
}

export interface AppointmentCreateDto {
  advisorId: number;
  appointmentDate: string;
  durationMinutes: number;
  sessionType: 'INITIAL_CONSULTATION' | 'STANDARD_SESSION' | 'FOLLOWUP_SESSION' | 'PLAN_REVIEW';
  sharedPlanId?: number;
  userNotes?: string;
}

export interface TimeSlot {
  date: string;
  startTime: string;
  endTime: string;
}

export interface AuthUrlResponse {
  authUrl: string;
}

export interface OAuthStatusResponse {
  authorized: boolean;
}

export interface OAuthCallbackResponse {
  message: string;
}