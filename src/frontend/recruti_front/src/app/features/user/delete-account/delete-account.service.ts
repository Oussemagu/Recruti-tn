import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DeleteAccountService {
  private verified = false;

  setVerified(value: boolean) {
    this.verified = value;
  }

  isVerified(): boolean {
    return this.verified;
  }

  clear() {
    this.verified = false;
  }
}
