export type CustomerStatus = 'PENDING' | 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'DELETED';
export type VehicleStatus = 'PENDING' | 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' | 'ORPHANED' | 'SOLD';
export type FuelType = 'DIESEL' | 'PETROL' | 'ELECTRIC' | 'HYBRID' | 'LPG';
export type MaintenanceStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED' | 'OVERDUE';
export type MaintenanceIntervalType = 'KM_BASED' | 'TIME_BASED';
export type MaintenanceType =
  | 'PREVENTIVE'
  | 'CORRECTIVE'
  | 'TECHNICAL_INSPECTION'
  | 'OIL_CHANGE'
  | 'TIRE_CHANGE'
  | 'BRAKE_CHECK'
  | 'BATTERY_REPLACEMENT'
  | 'GENERAL_OVERHAUL';
export type InvoiceStatus = 'DRAFT' | 'SENT' | 'PAID' | 'OVERDUE' | 'CANCELLED' | 'REFUNDED';
export type PaymentMethod = 'CARD' | 'TRANSFER' | 'DIRECT_DEBIT' | 'CASH';
export type PlanType = 'BASIC' | 'PRO' | 'ENTERPRISE';
export type SubscriptionStatus = 'PENDING' | 'ACTIVE' | 'CANCELLED' | 'EXPIRED';
export type TransactionStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';
export type EntityType = 'CUSTOMER' | 'VEHICLE' | 'PAYMENT';
export type DocumentType =
  | 'INSURANCE'
  | 'LICENSE'
  | 'CONTRACT'
  | 'INVOICE'
  | 'VEHICLE_PHOTO'
  | 'TECHNICAL_REPORT'
  | 'REGISTRATION_CERTIFICATE'
  | 'OTHER';
export type DocumentStatus = 'ACTIVE' | 'EXPIRED' | 'ARCHIVED' | 'PAID';
export type NotificationChannel = 'EMAIL' | 'SMS' | 'PUSH' | 'IN_APP';
export type NotificationType =
  | 'MAINTENANCE_DUE'
  | 'MAINTENANCE_OVERDUE'
  | 'MAINTENANCE_COMPLETED'
  | 'VEHICLE_STATUS_CHANGED'
  | 'INSURANCE_EXPIRING'
  | 'TECHNICAL_INSPECTION_DUE'
  | 'CUSTOMER_VALIDATED'
  | 'CUSTOMER_REJECTED'
  | 'INVOICE_CREATED'
  | 'INVOICE_PAID'
  | 'INVOICE_OVERDUE'
  | 'SUBSCRIPTION_RENEWAL'
  | 'WELCOME'
  | 'ALERT_CRITICAL'
  | 'ALERT_WARNING'
  | 'ALERT_INFO';
export type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED' | 'DELIVERED' | 'READ';

export interface Address {
  street?: string;
  city?: string;
  zipCode?: string;
  country?: string;
}

export interface Customer {
  id: string;
  companyName: string;
  vatNumber: string;
  email: string;
  phone?: string;
  address?: Address;
  contactFirstName?: string;
  contactLastName?: string;
  contactEmail?: string;
  contactPhone?: string;
  status: CustomerStatus;
  createdAt?: string;
}

export interface CustomerRequest {
  companyName: string;
  vatNumber: string;
  email: string;
  phone?: string;
  address?: Address;
  contactFirstName?: string;
  contactLastName?: string;
  contactEmail?: string;
  contactPhone?: string;
}

export interface Vehicle {
  id: string;
  plateNumber: string;
  vin: string;
  brand: string;
  model: string;
  year: number;
  color?: string;
  status: VehicleStatus;
  fuelType?: FuelType;
  mileage?: number;
  registrationDate?: string;
  insuranceExpiryDate?: string;
  technicalInspectionDate?: string;
  customerId?: string;
  gpsDeviceId?: string;
}

export interface VehicleRequest {
  plateNumber: string;
  vin: string;
  brand: string;
  model: string;
  year: number;
  color?: string;
  fuelType?: FuelType | '';
  mileage?: number;
  registrationDate?: string;
  insuranceExpiryDate?: string;
  technicalInspectionDate?: string;
  customerId?: string;
  gpsDeviceId?: string;
}

export interface Maintenance {
  id: string;
  vehicleId: string;
  vehiclePlateNumber?: string;
  type: MaintenanceType;
  description: string;
  scheduledDate: string;
  completedDate?: string;
  status: MaintenanceStatus;
  estimatedCost?: number;
  actualCost?: number;
  mileageAtMaintenance?: number;
  garageName?: string;
  technicianName?: string;
  notes?: string;
  overdue?: boolean;
}

export interface MaintenanceRequest {
  type: MaintenanceType;
  description: string;
  scheduledDate: string;
  estimatedCost?: number;
  garageName?: string;
  garageContact?: string;
  partsUsed?: string;
  technicianName?: string;
  notes?: string;
}

export interface MaintenanceAlert {
  maintenanceId: string;
  vehicleId: string;
  vehiclePlateNumber?: string;
  type: MaintenanceType;
  description: string;
  scheduledDate: string;
  daysUntilDue?: number;
  overdueDays?: number;
  alertLevel?: string;
  message?: string;
}

export interface MaintenancePlan {
  id: string;
  vehicleId: string;
  name: string;
  intervalType: MaintenanceIntervalType;
  intervalValue: number;
  lastDoneDate?: string;
  lastDoneMileage?: number;
  nextDueDate?: string;
  nextDueMileage?: number;
  alertDaysBefore?: number;
  isActive?: boolean;
}

export interface MaintenancePlanRequest {
  name: string;
  intervalType: MaintenanceIntervalType;
  intervalValue: number;
  alertDaysBefore?: number;
}

export interface Invoice {
  id: string;
  invoiceNumber: string;
  customerId: string;
  vehicleId?: string;
  subscriptionId?: string;
  amount: number;
  currency: string;
  description: string;
  status: InvoiceStatus;
  issueDate?: string;
  dueDate?: string;
  paidAt?: string;
}

export interface InvoiceRequest {
  customerId: string;
  vehicleId?: string;
  subscriptionId?: string;
  amount: number;
  currency: string;
  description: string;
}

export interface Subscription {
  id: string;
  customerId: string;
  planType: PlanType;
  monthlyAmount: number;
  startDate?: string;
  endDate?: string;
  status: SubscriptionStatus;
  autoRenew?: boolean;
}

export interface Transaction {
  id: string;
  invoiceId: string;
  amount: number;
  paymentMethod: PaymentMethod;
  status: TransactionStatus;
  externalReference?: string;
  transactionDate?: string;
}

export interface FleetDocument {
  id: string;
  fileName: string;
  originalName: string;
  contentType: string;
  size: number;
  documentType: DocumentType;
  entityType: EntityType;
  entityId: string;
  uploadedBy?: string;
  uploadedAt?: string;
  expiryDate?: string;
  status: DocumentStatus;
}

export interface NotificationItem {
  id: string;
  recipientId: string;
  recipientEmail?: string;
  type: NotificationType;
  channel: NotificationChannel;
  subject: string;
  content: string;
  status: NotificationStatus;
  sentAt?: string;
  createdAt?: string;
}
