import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { AuthResponse } from '../../../core/models/auth.model';

function matchPasswords(ctrl: AbstractControl): ValidationErrors | null {
  const p = ctrl.get('password')?.value;
  const c = ctrl.get('confirm')?.value;
  return p && c && p !== c ? { mismatch: true } : null;
}

@Component({
  selector: 'app-register',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit {
  form!: FormGroup;
  submitting = false;
  errorMsg = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      organizationName: ['', [Validators.required, Validators.minLength(2)]],
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirm: ['', [Validators.required]],
    }, { validators: matchPasswords });
  }

  get f(): { [key: string]: AbstractControl } {
  return this.form.controls as { [key: string]: AbstractControl };
}

  onSubmit(): void {
    this.errorMsg = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { organizationName, fullName, email, password } = this.form.value;
    this.submitting = true;

    this.auth.register(organizationName!, email!, password!, fullName!).subscribe({
      next: (res: AuthResponse) => {
        // Token is automatically handled by auth.service.ts
        this.submitting = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err: Error) => {
        this.submitting = false;
        this.errorMsg = err?.message || 'Error al registrarse';
      }
    });
  }
}
