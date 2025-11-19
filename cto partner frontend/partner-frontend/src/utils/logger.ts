// Comprehensive logging system for Restaurant Partner App
export enum LogLevel {
  ERROR = 'error',
  WARN = 'warn',
  INFO = 'info',
  DEBUG = 'debug',
  TRACE = 'trace',
}

export interface LogEntry {
  level: LogLevel;
  message: string;
  data?: Record<string, any>;
  timestamp: string;
  userId?: string;
  sessionId?: string;
  url?: string;
  userAgent?: string;
  component?: string;
  function?: string;
  error?: Error;
}

export interface LoggerConfig {
  level: LogLevel;
  enableConsole: boolean;
  enableStorage: boolean;
  enableRemote: boolean;
  maxStoredLogs: number;
  enablePerformanceLogging: boolean;
  redactSensitiveData: boolean;
}

class Logger {
  private config: LoggerConfig;
  private sessionId: string;
  private isOnline: boolean = true;

  constructor(config: Partial<LoggerConfig> = {}) {
    this.config = {
      level: LogLevel.INFO,
      enableConsole: true,
      enableStorage: true,
      enableRemote: false, // Set to true when monitoring service is configured
      maxStoredLogs: 1000,
      enablePerformanceLogging: true,
      redactSensitiveData: true,
      ...config,
    };

    this.sessionId = this.generateSessionId();
    this.initializeNetworkListener();
  }

  private generateSessionId(): string {
    return `session_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  private initializeNetworkListener() {
    if (typeof window !== 'undefined') {
      window.addEventListener('online', () => {
        this.isOnline = true;
        this.syncStoredLogs();
      });

      window.addEventListener('offline', () => {
        this.isOnline = false;
      });
    }
  }

  private shouldLog(level: LogLevel): boolean {
    const levelOrder = {
      [LogLevel.TRACE]: 0,
      [LogLevel.DEBUG]: 1,
      [LogLevel.INFO]: 2,
      [LogLevel.WARN]: 3,
      [LogLevel.ERROR]: 4,
    };

    return levelOrder[level] >= levelOrder[this.config.level];
  }

  private redactSensitiveData(data: any): any {
    if (!this.config.redactSensitiveData || !data) return data;

    const sensitiveKeys = [
      'password',
      'token',
      'apiKey',
      'secret',
      'creditCard',
      'ssn',
      'phone',
      'email',
    ];

    const redacted = { ...data };
    
    const redactValue = (obj: any, key: string, value: any) => {
      if (typeof value === 'string' && value.length > 0) {
        obj[key] = '*'.repeat(Math.min(8, value.length));
      } else {
        obj[key] = '[REDACTED]';
      }
    };

    for (const key in redacted) {
      if (sensitiveKeys.some(sensitive => key.toLowerCase().includes(sensitive))) {
        redactValue(redacted, key, redacted[key]);
      } else if (typeof redacted[key] === 'object' && redacted[key] !== null) {
        redacted[key] = this.redactSensitiveData(redacted[key]);
      }
    }

    return redacted;
  }

  private createLogEntry(
    level: LogLevel,
    message: string,
    data?: Record<string, any>,
    error?: Error,
    component?: string,
    functionName?: string
  ): LogEntry {
    const timestamp = new Date().toISOString();
    
    const logData = data ? this.redactSensitiveData(data) : undefined;
    
    const entry: LogEntry = {
      level,
      message,
      data: logData,
      timestamp,
      sessionId: this.sessionId,
      url: typeof window !== 'undefined' ? window.location.href : undefined,
      userAgent: typeof navigator !== 'undefined' ? navigator.userAgent : undefined,
      component,
      function: functionName,
      error,
    };

    return entry;
  }

  private formatForConsole(entry: LogEntry): string {
    const { level, message, timestamp, component, function: func } = entry;
    const prefix = `[${timestamp}] [${level.toUpperCase()}]`;
    const context = component && func ? `[${component}.${func}]` : '';
    
    return `${prefix} ${context} ${message}`.trim();
  }

  private storeLog(entry: LogEntry): void {
    if (!this.config.enableStorage) return;

    try {
      const existingLogs = JSON.parse(localStorage.getItem('appLogs') || '[]');
      existingLogs.push(entry);
      
      // Keep only the most recent logs
      if (existingLogs.length > this.config.maxStoredLogs) {
        existingLogs.splice(0, existingLogs.length - this.config.maxStoredLogs);
      }
      
      localStorage.setItem('appLogs', JSON.stringify(existingLogs));
    } catch (error) {
      console.error('Failed to store log entry:', error);
    }
  }

  private async syncStoredLogs(): Promise<void> {
    if (!this.config.enableRemote || !this.isOnline) return;

    try {
      const storedLogs = JSON.parse(localStorage.getItem('appLogs') || '[]');
      
      if (storedLogs.length === 0) return;

      // In a real app, send to monitoring service
      // await this.sendToRemoteService(storedLogs);
      
      // Clear stored logs after successful sync
      localStorage.removeItem('appLogs');
      
      this.info('Successfully synced logs to remote service', {
        count: storedLogs.length,
      });
    } catch (error) {
      this.error('Failed to sync logs to remote service', { error });
    }
  }

  private sendToRemoteService(logs: LogEntry[]): Promise<void> {
    return new Promise((resolve, reject) => {
      // Example implementation - replace with actual service
      fetch('/api/logs', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ logs }),
      })
      .then(response => {
        if (response.ok) {
          resolve();
        } else {
          reject(new Error(`HTTP ${response.status}`));
        }
      })
      .catch(reject);
    });
  }

  // Public logging methods
  error(
    message: string,
    data?: Record<string, any>,
    error?: Error,
    component?: string,
    functionName?: string
  ): void {
    if (!this.shouldLog(LogLevel.ERROR)) return;

    const entry = this.createLogEntry(
      LogLevel.ERROR,
      message,
      data,
      error,
      component,
      functionName
    );

    if (this.config.enableConsole) {
      console.error(this.formatForConsole(entry), error, data);
    }

    this.storeLog(entry);
  }

  warn(
    message: string,
    data?: Record<string, any>,
    component?: string,
    functionName?: string
  ): void {
    if (!this.shouldLog(LogLevel.WARN)) return;

    const entry = this.createLogEntry(
      LogLevel.WARN,
      message,
      data,
      undefined,
      component,
      functionName
    );

    if (this.config.enableConsole) {
      console.warn(this.formatForConsole(entry), data);
    }

    this.storeLog(entry);
  }

  info(
    message: string,
    data?: Record<string, any>,
    component?: string,
    functionName?: string
  ): void {
    if (!this.shouldLog(LogLevel.INFO)) return;

    const entry = this.createLogEntry(
      LogLevel.INFO,
      message,
      data,
      undefined,
      component,
      functionName
    );

    if (this.config.enableConsole) {
      console.info(this.formatForConsole(entry), data);
    }

    this.storeLog(entry);
  }

  debug(
    message: string,
    data?: Record<string, any>,
    component?: string,
    functionName?: string
  ): void {
    if (!this.shouldLog(LogLevel.DEBUG)) return;

    const entry = this.createLogEntry(
      LogLevel.DEBUG,
      message,
      data,
      undefined,
      component,
      functionName
    );

    if (this.config.enableConsole) {
      console.debug(this.formatForConsole(entry), data);
    }

    this.storeLog(entry);
  }

  trace(
    message: string,
    data?: Record<string, any>,
    component?: string,
    functionName?: string
  ): void {
    if (!this.shouldLog(LogLevel.TRACE)) return;

    const entry = this.createLogEntry(
      LogLevel.TRACE,
      message,
      data,
      undefined,
      component,
      functionName
    );

    if (this.config.enableConsole) {
      console.trace(this.formatForConsole(entry), data);
    }

    this.storeLog(entry);
  }

  // Performance logging
  startTimer(name: string): () => number {
    const startTime = Date.now();
    
    return () => {
      const duration = Date.now() - startTime;
      this.debug(`Timer '${name}' completed`, { duration: `${duration}ms` });
      return duration;
    };
  }

  measureFunction<T>(
    name: string,
    fn: () => T,
    component?: string,
    functionName?: string
  ): T {
    const timer = this.startTimer(name);
    const result = fn();
    
    if (typeof result === 'object' && result !== null && 'then' in result) {
      // Handle promise
      return (result as any).then((value: any) => {
        timer();
        return value;
      }) as T;
    }
    
    timer();
    return result;
  }

  measureAsyncFunction<T>(
    name: string,
    fn: () => Promise<T>,
    component?: string,
    functionName?: string
  ): Promise<T> {
    return this.measureFunction(name, fn, component, functionName) as Promise<T>;
  }

  // User interaction logging
  logUserAction(
    action: string,
    data?: Record<string, any>,
    component?: string
  ): void {
    this.info(`User action: ${action}`, { ...data, actionType: 'user_interaction' }, component);
  }

  // API request logging
  logApiRequest(
    method: string,
    url: string,
    data?: Record<string, any>
  ): void {
    this.info(`API Request: ${method} ${url}`, data, 'HttpClient', 'request');
  }

  logApiResponse(
    method: string,
    url: string,
    status: number,
    duration: number,
    data?: Record<string, any>
  ): void {
    const level = status >= 400 ? LogLevel.ERROR : LogLevel.INFO;
    if (level === LogLevel.ERROR) {
      this.error(`API Response: ${method} ${url}`,
        { ...data, status, duration: `${duration}ms` },
        undefined, 'HttpClient', 'response'
      );
    } else {
      this.info(`API Response: ${method} ${url}`,
        { ...data, status, duration: `${duration}ms` },
        'HttpClient', 'response'
      );
    }
  }

  // Navigation logging
  logNavigation(
    from: string,
    to: string,
    params?: Record<string, any>
  ): void {
    this.info(`Navigation: ${from} → ${to}`, params, 'Navigation');
  }

  // Error logging helper
  logError(
    error: Error,
    context: string,
    additionalData?: Record<string, any>
  ): void {
    this.error(`Error in ${context}`, additionalData, error, context);
  }

  // Utility methods
  clearStoredLogs(): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem('appLogs');
    }
  }

  getStoredLogs(): LogEntry[] {
    try {
      return JSON.parse(localStorage.getItem('appLogs') || '[]');
    } catch {
      return [];
    }
  }

  exportLogs(): string {
    const logs = this.getStoredLogs();
    return JSON.stringify(logs, null, 2);
  }

  updateConfig(newConfig: Partial<LoggerConfig>): void {
    this.config = { ...this.config, ...newConfig };
  }

  getConfig(): LoggerConfig {
    return { ...this.config };
  }
}

// Create singleton logger instance
const logger = new Logger({
  level: __DEV__ ? LogLevel.DEBUG : LogLevel.INFO,
  enableConsole: true,
  enableStorage: true,
  enableRemote: false, // Enable when monitoring service is configured
  maxStoredLogs: 1000,
  enablePerformanceLogging: true,
  redactSensitiveData: true,
});

// Convenience functions for easy use
export const logError = (
  message: string,
  data?: Record<string, any>,
  error?: Error,
  component?: string,
  functionName?: string
) => logger.error(message, data, error, component, functionName);

export const logWarn = (
  message: string,
  data?: Record<string, any>,
  component?: string,
  functionName?: string
) => logger.warn(message, data, component, functionName);

export const logInfo = (
  message: string,
  data?: Record<string, any>,
  component?: string,
  functionName?: string
) => logger.info(message, data, component, functionName);

export const logDebug = (
  message: string,
  data?: Record<string, any>,
  component?: string,
  functionName?: string
) => logger.debug(message, data, component, functionName);

export const logTrace = (
  message: string,
  data?: Record<string, any>,
  component?: string,
  functionName?: string
) => logger.trace(message, data, component, functionName);

export const measureFunction = <T>(
  name: string,
  fn: () => T,
  component?: string,
  functionName?: string
) => logger.measureFunction(name, fn, component, functionName);

export const measureAsyncFunction = <T>(
  name: string,
  fn: () => Promise<T>,
  component?: string,
  functionName?: string
) => logger.measureAsyncFunction(name, fn, component, functionName);

export { logger };
export default logger;