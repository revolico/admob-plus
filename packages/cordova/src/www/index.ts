import * as ads from './ads';
import {start} from './ads/base';
import {AdMobConfig, execAsync} from './common';
import {Events} from './shared';

export * from './ads';

export class AdMob {
  public readonly AppOpenAd = ads.AppOpenAd;
  public readonly BannerAd = ads.BannerAd;
  public readonly InterstitialAd = ads.InterstitialAd;
  public readonly NativeAd = ads.NativeAd;
  public readonly RewardedAd = ads.RewardedAd;
  public readonly RewardedInterstitialAd = ads.RewardedInterstitialAd;
  public readonly WebViewAd = ads.WebViewAd;

  public readonly Events = Events;

  configure(config: AdMobConfig) {
    return execAsync('configure', [config]);
  }

  public start() {
    return start();
  }
}

declare global {
  const admob: AdMob;
}

export default AdMob;
